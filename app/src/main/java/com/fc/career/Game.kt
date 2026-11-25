package com.fc.career

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.*
import kotlin.random.Random

class P(var name: String, var pos: String, var ovr: Int, var age: Int)
class C(val id: Int, var name: String, var lg: Int, val rep: Int, var budget: Long) {
    val ps = mutableListOf<P>()
    var pts = 0; var w = 0; var d = 0; var l = 0; var gf = 0; var ga = 0
    fun reset() { pts = 0; w = 0; d = 0; l = 0; gf = 0; ga = 0 }
}
class L(val name: String, val tier: Int, val country: String)
class Ev(val m: Int, val t: String, val home: Boolean, val goal: Boolean)
class Res(val h: Int, val a: Int, val gh: Int, val ga: Int, val ev: List<Ev>)

private val SUF = listOf("Sporting", "Athletic", "Town", "Rovers", "Albion", "Wanderers", "Borough", "Harriers", "Dynamo", "Rangers", "Olympic", "Forest")

fun worth(p: P): Long {
    var v = max(1, p.ovr - 45).toDouble().pow(3) * 1.6
    if (p.age > 30) v *= .6
    if (p.age < 22) v *= 1.25
    return max(50L, v.toLong())
}
fun eur(k: Long): String = if (k >= 1000) "€" + String.format("%.1f", k / 1000.0) + "M" else "€${k}K"
fun bud(rep: Int): Long = (rep.toDouble().pow(3) * 0.18).toLong()
fun strs(a: JSONArray) = List(a.length()) { a.getString(it) }

object G {
    lateinit var ctx: Context
    val ver = mutableIntStateOf(0)
    fun bump() { ver.intValue++ }
    val leagues = mutableListOf<L>()
    val clubs = mutableListOf<C>()
    val nf = mutableListOf<List<String>>()
    val nl = mutableListOf<List<String>>()
    val news = mutableListOf<String>()
    var offers = mutableListOf<Int>()
    var mode = 0; var coach = "Coach"; var lic = 0; var crep = 15; var me = -1
    var week = 0; var season = 2026; var ment = 1
    var fix: List<List<Pair<Int, Int>>> = listOf()
    val licN = listOf("Euro D", "Euro C", "Euro B", "Euro A", "Euro Pro")
    val story = listOf(
        "LULUS: Lisensi Euro D diraih. Kamu resmi boleh melatih tim junior.",
        "LULUS: Lisensi Euro C. Ilmu taktik dasarmu diakui federasi.",
        "LULUS: Lisensi Euro B. Nama kamu mulai disebut klub-klub kecil.",
        "LULUS: Lisensi Euro A. Kamu kini layak melatih tim profesional.",
        "LULUS: Lisensi Euro Pro! Semua lisensi lengkap, saatnya cari klub pertamamu."
    )

    fun boot(c: Context) {
        ctx = c
        leagues.clear(); clubs.clear(); nf.clear(); nl.clear()
        val j = JSONObject(c.assets.open("db.json").bufferedReader().readText())
        val cs = j.getJSONArray("c")
        for (ci in 0 until cs.length()) {
            val o = cs.getJSONObject(ci)
            val f = strs(o.getJSONArray("f")); val l = strs(o.getJSONArray("l"))
            nf.add(f); nl.add(l)
            val lg = strs(o.getJSONArray("lg")); val t1 = strs(o.getJSONArray("t1")); val ct = strs(o.getJSONArray("ct"))
            val b = o.getInt("b")
            for (t in 0..2) {
                leagues.add(L(lg[t], t + 1, o.getString("n")))
                for (i in 0 until 12) {
                    val name = if (t == 0) t1[i] else ct[(t - 1) * 12 + i] + " " + SUF[(i + t) % SUF.size]
                    val r = when (t) { 0 -> b - i * 2.5; 1 -> b - 34 - i * 1.5; else -> b - 44 - i * 1.2 }
                    val rep = r.roundToInt().coerceAtLeast(5)
                    val cl = C(clubs.size, name, ci * 3 + t, rep, bud(rep))
                    gen(cl, f, l)
                    clubs.add(cl)
                }
            }
        }
        val s = j.getJSONArray("s")
        for (i in 0 until s.length()) {
            val a = s.getJSONArray(i)
            val cl = clubs.firstOrNull { it.name == a.getString(1) } ?: continue
            val pos = a.getString(2)
            val w = cl.ps.filter { it.pos == pos }.minByOrNull { it.ovr }
            if (w != null) cl.ps.remove(w)
            cl.ps.add(P(a.getString(0), pos, a.getInt(3), a.getInt(4)))
        }
        try {
            val sq = JSONObject(c.assets.open("squads.json").bufferedReader().readText())
            for (k in sq.keys()) {
                val cl = clubs.firstOrNull { it.name == k } ?: continue
                val arr = sq.getJSONArray(k)
                if (arr.length() < 11) continue
                cl.ps.clear()
                for (i in 0 until arr.length()) { val q = arr.getString(i).split("|"); cl.ps.add(P(q[0], q[1], q[2].toInt(), q[3].toInt())) }
                val ci = cl.lg / 3; val ex = listOf("DEF", "MID", "FWD", "MID", "DEF")
                var n = 0
                while (cl.ps.size < 18) { cl.ps.add(P(nf[ci].random() + " " + nl[ci].random(), ex[n % ex.size], (30 + cl.rep * 0.5 + Random.nextInt(-6, 4)).roundToInt().coerceIn(30, 90), Random.nextInt(18, 33))); n++ }
            }
        } catch (e: Exception) { }
        fixtures()
    }

    fun gen(c: C, f: List<String>, l: List<String>) {
        listOf("GK", "GK", "DEF", "DEF", "DEF", "DEF", "DEF", "DEF", "MID", "MID", "MID", "MID", "MID", "MID", "FWD", "FWD", "FWD", "FWD").forEach { pos ->
            c.ps.add(P(f.random() + " " + l.random(), pos, (30 + c.rep * 0.5 + Random.nextInt(-6, 9)).roundToInt().coerceIn(30, 90), Random.nextInt(17, 36)))
        }
    }

    fun rounds(ids: List<Int>): List<List<Pair<Int, Int>>> {
        val n = ids.size; val a = ids.toMutableList(); val r = mutableListOf<List<Pair<Int, Int>>>()
        for (i in 0 until n - 1) {
            val m = mutableListOf<Pair<Int, Int>>()
            for (j in 0 until n / 2) { val h = a[j]; val aw = a[n - 1 - j]; m.add(if ((i + j) % 2 == 0) h to aw else aw to h) }
            r.add(m); a.add(1, a.removeAt(n - 1))
        }
        return r + r.map { x -> x.map { it.second to it.first } }
    }

    fun fixtures() {
        val per = leagues.indices.map { li -> rounds(clubs.filter { it.lg == li }.map { it.id }) }
        fix = (0 until 22).map { w -> per.flatMap { it[w] } }
    }

    fun ms(c: C): Double {
        fun t(pos: String, k: Int) = c.ps.filter { it.pos == pos }.sortedByDescending { it.ovr }.take(k).map { it.ovr }
        val x = t("GK", 1) + t("DEF", 4) + t("MID", 3) + t("FWD", 3)
        return if (x.isEmpty()) 40.0 else x.average()
    }
    fun poi(lam: Double): Int { val lim = exp(-lam); var k = 0; var p = 1.0; do { k++; p *= Random.nextDouble() } while (p > lim); return k - 1 }
    fun lam(a: Double, b: Double, home: Boolean, m: Double) = (1.3 * exp((a - b) / 12.0) * (if (home) 1.12 else 1.0) * m).coerceIn(0.15, 5.0)
    fun rec(h: C, a: C, gh: Int, ga: Int) {
        h.gf += gh; h.ga += ga; a.gf += ga; a.ga += gh
        if (gh > ga) { h.w++; h.pts += 3; a.l++ } else if (gh < ga) { a.w++; a.pts += 3; h.l++ } else { h.d++; a.d++; h.pts++; a.pts++ }
    }
    fun tbl(li: Int) = clubs.filter { it.lg == li }.sortedWith(compareByDescending<C> { it.pts }.thenByDescending { it.gf - it.ga }.thenByDescending { it.gf })

    fun events(h: C, a: C, gh: Int, ga: Int): List<Ev> {
        val e = mutableListOf<Ev>()
        fun scorer(c: C): String {
            val w = c.ps.filter { it.pos != "GK" }.flatMap { p -> List(if (p.pos == "FWD") 4 else if (p.pos == "MID") 2 else 1) { p } }
            return w.random().name
        }
        repeat(gh) { e.add(Ev(Random.nextInt(1, 91), scorer(h), true, true)) }
        repeat(ga) { e.add(Ev(Random.nextInt(1, 91), scorer(a), false, true)) }
        repeat(Random.nextInt(1, 4)) { val hm = Random.nextBoolean(); e.add(Ev(Random.nextInt(1, 91), (if (hm) h else a).ps.random().name, hm, false)) }
        return e.sortedBy { it.m }
    }

    fun startMatch(): Res? {
        var mine: Res? = null
        val mm = listOf(.8, 1.0, 1.2)[ment]; val mo = listOf(.7, 1.0, 1.2)[ment]
        for ((h, a) in fix[week]) {
            val hc = clubs[h]; val ac = clubs[a]
            if (h == me || a == me) {
                val gh = poi(lam(ms(hc), ms(ac), true, if (h == me) mm else mo))
                val ga = poi(lam(ms(ac), ms(hc), false, if (a == me) mm else mo))
                mine = Res(h, a, gh, ga, events(hc, ac, gh, ga))
            } else rec(hc, ac, poi(lam(ms(hc), ms(ac), true, 1.0)), poi(lam(ms(ac), ms(hc), false, 1.0)))
        }
        return mine
    }

    fun finish(r: Res) {
        rec(clubs[r.h], clubs[r.a], r.gh, r.ga)
        news.add(0, "HASIL: ${clubs[r.h].name} ${r.gh}-${r.ga} ${clubs[r.a].name}")
        week++; world()
        if (week >= 22) endSeason()
        bump()
    }

    fun world() {
        if (Random.nextInt(100) < 35) {
            val a = clubs.random(); val b = clubs.random()
            if (a.id != b.id && a.id != me && b.id != me && a.ps.size > 16 && b.ps.size < 26) {
                val p = a.ps.filter { it.ovr >= 55 }.randomOrNull()
                if (p != null) {
                    val fee = worth(p)
                    if (b.budget >= fee) {
                        a.ps.remove(p); b.ps.add(p); a.budget += fee; b.budget -= fee
                        news.add(0, "DONE DEAL: ${p.name} (${p.ovr}) pindah dari ${a.name} ke ${b.name} seharga ${eur(fee)}")
                    }
                }
            }
        }
        if (Random.nextInt(100) < 30) {
            val c = clubs.random(); val k = c.lg / 3
            news.add(0, "DONE DEAL: ${nf[k].random()} ${nl[k].random()} ditunjuk jadi pelatih ${c.name}")
        }
        while (news.size > 60) news.removeAt(news.size - 1)
    }

    fun endSeason() {
        val nC = leagues.size / 3
        val tabs = leagues.indices.map { tbl(it) }
        if (me >= 0) {
            val li = clubs[me].lg
            val rank = tabs[li].indexOf(clubs[me]) + 1
            val exp = clubs.filter { it.lg == li }.sortedByDescending { it.rep }.indexOf(clubs[me]) + 1
            news.add(0, "MUSIM SELESAI: ${clubs[me].name} finis peringkat $rank (target $exp)")
            if (mode == 1) {
                crep = (crep + ((exp - rank) * 1.5).roundToInt() + (if (rank == 1) 4 else 0)).coerceIn(1, 95)
                genOffers()
            }
        }
        leagues.indices.filter { leagues[it].tier == 1 }.forEach { news.add(0, "JUARA ${leagues[it].name} (${leagues[it].country}): ${tabs[it][0].name}") }
        for (c in 0 until nC) for (t in 0..1) {
            tabs[c * 3 + t + 1].take(2).forEach { it.lg = c * 3 + t }
            tabs[c * 3 + t].takeLast(2).forEach { it.lg = c * 3 + t + 1 }
        }
        clubs.forEach { c ->
            c.reset(); c.budget += bud(c.rep) / 2
            c.ps.forEach { p ->
                p.age++
                p.ovr = (p.ovr + (if (p.age < 24) Random.nextInt(0, 5) else if (p.age < 30) Random.nextInt(-1, 3) else Random.nextInt(-4, 1))).coerceIn(30, 95)
            }
        }
        season++; week = 0; fixtures()
    }

    fun genOffers() {
        offers = clubs.filter { it.id != me && it.rep <= crep + 10 && it.rep >= crep - 8 }.shuffled().take(3).map { it.id }.toMutableList()
        if (offers.isEmpty()) offers = clubs.filter { it.id != me }.sortedBy { it.rep }.take(3).map { it.id }.toMutableList()
        news.add(0, "TAWARAN: ${offers.joinToString { clubs[it].name }} tertarik merekrut $coach")
    }
    fun accept(id: Int) { me = id; offers.clear(); news.add(0, "DONE DEAL: $coach resmi jadi pelatih ${clubs[id].name}!"); bump() }
    fun course() { news.add(0, story[lic]); lic++; if (lic >= 5) genOffers(); bump() }

    fun ask(p: P) = (worth(p) * (0.9 + abs(p.name.hashCode() % 35) / 100.0)).toLong()
    fun reply(o: Long, a: Long) = if (o >= a * 0.97) 2 else if (o >= a * 0.85) 1 else 0
    fun market(pos: String): List<Pair<P, C>> {
        val lim = clubs[me].budget * 13 / 10
        return clubs.filter { it.id != me }.flatMap { c -> c.ps.filter { pos == "ALL" || it.pos == pos }.map { it to c } }
            .filter { ask(it.first) <= lim }.sortedByDescending { it.first.ovr }.take(60)
    }
    fun buy(p: P, from: C, fee: Long): Boolean {
        val m = clubs[me]
        if (m.budget < fee || m.ps.size >= 30 || from.ps.size <= 14) return false
        from.ps.remove(p); m.ps.add(p); m.budget -= fee; from.budget += fee
        news.add(0, "DONE DEAL: ${m.name} dapatkan ${p.name} dari ${from.name} (${eur(fee)})"); bump(); return true
    }
    fun bid(p: P): Pair<C, Long>? {
        val f = (worth(p) * Random.nextDouble(.75, 1.2)).toLong()
        val b = clubs.filter { it.id != me && it.budget >= f && it.ps.size < 28 }.randomOrNull() ?: return null
        return b to f
    }
    fun sell(p: P, b: C, f: Long) {
        val m = clubs[me]; if (m.ps.size <= 14) return
        m.ps.remove(p); b.ps.add(p); m.budget += f; b.budget -= f
        news.add(0, "DONE DEAL: ${p.name} dijual ke ${b.name} (${eur(f)})"); bump()
    }

    fun newGame(m: Int, name: String, club: Int) {
        boot(ctx)
        news.clear(); offers.clear()
        mode = m; coach = name.ifBlank { "Coach" }; lic = if (m == 0) 5 else 0; crep = 15; me = club; week = 0; season = 2026; ment = 1
        news.add(0, if (m == 0) "SELAMAT DATANG: $coach resmi jadi manajer ${clubs[club].name}" else "BERITA: $coach memulai karir kepelatihan dari nol. Ambil Lisensi Euro D dulu!")
        bump()
    }

    private val file: File get() = File(ctx.filesDir, "save.json")
    fun hasSave() = file.exists()
    fun save() {
        val o = JSONObject()
        o.put("mode", mode).put("coach", coach).put("lic", lic).put("crep", crep).put("me", me).put("week", week).put("season", season).put("ment", ment)
        o.put("news", JSONArray(news.take(40))); o.put("offers", JSONArray(offers))
        val ca = JSONArray()
        for (c in clubs) {
            val a = JSONArray(); a.put(c.lg); a.put(c.budget); a.put(c.pts); a.put(c.w); a.put(c.d); a.put(c.l); a.put(c.gf); a.put(c.ga)
            val pa = JSONArray(); for (p in c.ps) pa.put(JSONArray().put(p.name).put(p.pos).put(p.ovr).put(p.age))
            a.put(pa); ca.put(a)
        }
        o.put("cl", ca); file.writeText(o.toString())
    }
    fun load() {
        boot(ctx)
        val o = JSONObject(file.readText())
        mode = o.getInt("mode"); coach = o.getString("coach"); lic = o.getInt("lic"); crep = o.getInt("crep"); me = o.getInt("me")
        week = o.getInt("week"); season = o.getInt("season"); ment = o.getInt("ment")
        news.clear(); news.addAll(strs(o.getJSONArray("news")))
        val oa = o.getJSONArray("offers"); offers = (0 until oa.length()).map { oa.getInt(it) }.toMutableList()
        val ca = o.getJSONArray("cl")
        for (i in 0 until ca.length()) {
            val a = ca.getJSONArray(i); val c = clubs[i]
            c.lg = a.getInt(0); c.budget = a.getLong(1); c.pts = a.getInt(2); c.w = a.getInt(3); c.d = a.getInt(4); c.l = a.getInt(5); c.gf = a.getInt(6); c.ga = a.getInt(7)
            c.ps.clear(); val pa = a.getJSONArray(8)
            for (k in 0 until pa.length()) { val q = pa.getJSONArray(k); c.ps.add(P(q.getString(0), q.getString(1), q.getInt(2), q.getInt(3))) }
        }
        fixtures(); bump()
    }
}
