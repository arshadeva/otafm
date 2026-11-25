package com.fc.career

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

val GR = Color(0xFF4CFF8A)
val GOLD = Color(0xFFFFC857)
val RED = Color(0xFFFF5C5C)
val CARD = Color(0xFF15283F)
val MUTED = Color(0xFF9FB4CC)
val BG = Brush.verticalGradient(listOf(Color(0xFF0C1F3A), Color(0xFF061018)))

private val KITS = mapOf(
    "Manchester Blue" to "6CABDD,FFFFFF", "Manchester Red" to "DA291C,FFD700", "Merseyside Red" to "C8102E,00B2A9", "London Gunners" to "EF0107,FFFFFF",
    "London Blues" to "034694,FFFFFF", "North London Spurs" to "132257,FFFFFF", "Newcastle Pies" to "241F20,FFFFFF", "Birmingham Villans" to "670E36,95BFE5",
    "East London Irons" to "7A263A,1BB1E7", "Brightland Seagulls" to "0057B8,FFFFFF", "Merseyside Toffees" to "003399,FFFFFF", "Fulton Cottagers" to "FFFFFF,000000",
    "Bavaria Munich" to "DC052D,FFFFFF", "Dortmund Yellows" to "FDE100,000000", "Leverkusen Pills" to "E32221,000000", "Leipzig Bulls" to "DD0741,FFFFFF",
    "Frankfurt Eagles" to "E1000F,000000", "Stuttgart Reds" to "E32219,FFFFFF", "Wolfsburg Wolfs" to "65B32E,FFFFFF", "Gladbach Foals" to "000000,00A650",
    "Freiburg Black" to "000000,E1000F", "Bremen Green" to "1D9053,FFFFFF", "Hoffenheim Village" to "1961B5,FFFFFF", "Mainz Carnival" to "C3141E,FFFFFF",
    "Intero Milano" to "0068A8,000000", "Milano Rossi" to "FB090B,000000", "Juvenil Turin" to "FFFFFF,000000", "Napolo Partenope" to "12A0D7,FFFFFF",
    "Romo Giallorossi" to "8E1F2F,F0BC42", "Lazzio Biancocelesti" to "87D8F7,FFFFFF", "Atalanto Bergamo" to "1E71B8,000000", "Fiorentino Viola" to "5B2D8E,FFFFFF",
    "Bolonya Rossoblu" to "1A2F48,A21C26", "Torinno Granata" to "8B1D2A,FFFFFF", "Udinesi Friuli" to "FFFFFF,000000", "Genovo Grifone" to "A50034,0F2C5C",
    "Galatasarai Lions" to "FDB913,A90432", "Fenerbahca Canaries" to "FFED00,002D72", "Besiktash Eagles" to "000000,FFFFFF", "Trabzonsport" to "7B1E3A,00A9E0", "Basakshehir" to "F26F21,1B2A49",
    "Madrid Royal" to "FFFFFF,FEBE10", "Barcelono Blau" to "A50044,004D98", "Atletic Madrid" to "CB3524,272E61", "Sevillo FC" to "FFFFFF,D4001A", "Valencio" to "FFFFFF,000000",
    "Villarreal Sub" to "FBE100,005187", "Bilbao Lions" to "EE2523,FFFFFF", "Betis Verde" to "0BB363,FFFFFF",
    "Persijo Jakarta" to "F26522,FFFFFF", "Persibu Bandung" to "0057B8,FFFFFF", "Arima Malang" to "1F4EA3,F7A21B", "Persebayo Surabaya" to "1B9E4B,FFFFFF", "PSN Makassar" to "D4001A,FFFFFF"
)
fun hx(s: String) = Color(("FF$s").toLong(16))
fun kit(n: String): Pair<Color, Color> {
    val s = KITS[n]
    if (s != null) { val p = s.split(","); return hx(p[0]) to hx(p[1]) }
    val h = abs(n.hashCode())
    return Color.hsv((h % 360).toFloat(), 0.65f, 0.75f) to Color.hsv(((h / 7) % 360).toFloat(), 0.2f, 0.95f)
}
fun posColor(p: String) = when (p) { "GK" -> GOLD; "DEF" -> Color(0xFF4DA3FF); "MID" -> GR; else -> RED }
fun ovrColor(o: Int) = if (o >= 80) GR else if (o >= 70) Color(0xFFB5E61D) else if (o >= 60) GOLD else Color(0xFFFF8A4C)

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        G.boot(this)
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = GR)) { App() } }
    }
}

@Composable fun Badge(name: String, sz: Dp = 40.dp) {
    val (a, b) = kit(name); val h = abs(name.hashCode())
    val ini = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }
    Box(Modifier.size(sz), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val ht = size.height
            val path = Path().apply {
                when (h % 3) {
                    0 -> { moveTo(.08f * w, .05f * ht); lineTo(.92f * w, .05f * ht); lineTo(.92f * w, .55f * ht); cubicTo(.92f * w, .8f * ht, .62f * w, .92f * ht, .5f * w, .98f * ht); cubicTo(.38f * w, .92f * ht, .08f * w, .8f * ht, .08f * w, .55f * ht); close() }
                    1 -> addOval(Rect(.04f * w, .04f * ht, .96f * w, .96f * ht))
                    else -> { moveTo(.5f * w, .02f * ht); lineTo(.94f * w, .26f * ht); lineTo(.94f * w, .74f * ht); lineTo(.5f * w, .98f * ht); lineTo(.06f * w, .74f * ht); lineTo(.06f * w, .26f * ht); close() }
                }
            }
            clipPath(path) {
                drawRect(a)
                when ((h / 3) % 3) {
                    0 -> drawRect(b, Offset(0f, 0f), Size(w, .26f * ht))
                    1 -> drawRect(b, Offset(0f, .74f * ht), Size(w, .3f * ht))
                    else -> { drawRect(b, Offset(0f, 0f), Size(.2f * w, ht)); drawRect(b, Offset(.8f * w, 0f), Size(.2f * w, ht)) }
                }
            }
            drawPath(path, b, style = Stroke(w * 0.06f))
        }
        Text(ini, color = if (a.luminance() > 0.55f) Color.Black else Color.White, fontSize = (sz.value * 0.34f).sp, fontWeight = FontWeight.Black)
    }
}

@Composable fun Chip(t: String, c: Color) {
    Text(t, color = c, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(c.copy(alpha = .16f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp))
}
@Composable fun Panel(mod: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(mod.background(CARD, RoundedCornerShape(14.dp)).padding(10.dp), content = content)
}

@Composable fun App() {
    var scr by remember { mutableStateOf("menu") }
    var res by remember { mutableStateOf<Res?>(null) }
    var setup by remember { mutableIntStateOf(0) }
    val v = G.ver.intValue
    Box(Modifier.fillMaxSize().background(BG)) {
        when (scr) {
            "menu" -> Menu(onNew = { setup = it; scr = "setup" }, onCont = { scr = "hub" })
            "setup" -> Setup(setup) { scr = "hub" }
            "hub" -> Hub(v, onPlay = { res = G.startMatch(); if (res != null) scr = "match" }, onMenu = { scr = "menu" })
            else -> Match(res!!) { G.finish(res!!); scr = "hub" }
        }
    }
}

@Composable fun ModeCard(t: String, d: String, e: String, c: Color, f: () -> Unit) {
    Column(Modifier.width(270.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(c.copy(alpha = .85f), CARD))).clickable { f() }.padding(18.dp)) {
        Text(e, fontSize = 30.sp); Spacer(Modifier.height(6.dp))
        Text(t, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(d, fontSize = 12.sp, color = Color(0xFFD0DCEB))
    }
}

@Composable fun Menu(onNew: (Int) -> Unit, onCont: () -> Unit) {
    val tr = rememberInfiniteTransition(label = "g")
    val a by tr.animateFloat(0.25f, 0.9f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "a")
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Box(Modifier.background(Brush.radialGradient(listOf(GR.copy(alpha = a * .28f), Color.Transparent)), RoundedCornerShape(50)).padding(horizontal = 60.dp, vertical = 6.dp)) {
            Text("OTAFM", fontSize = 72.sp, fontWeight = FontWeight.Black, color = GR, letterSpacing = 10.sp)
        }
        Text("FOOTBALL MANAGER  •  CAREER MODE", fontSize = 12.sp, letterSpacing = 4.sp, color = GOLD)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            ModeCard("CAREER MANAGER", "Pilih klub impianmu, kelola skuad, transfer, dan raih trofi.", "🏟️", Color(0xFF1E88E5)) { onNew(0) }
            ModeCard("CAREER COACH", "Mulai dari lisensi Euro D, latih klub kecil, naik jadi pelatih klub besar.", "🧢", Color(0xFFE53935)) { onNew(1) }
        }
        if (G.hasSave()) { Spacer(Modifier.height(14.dp)); OutlinedButton(onClick = { G.load(); onCont() }) { Text("▶  Lanjutkan Karir") } }
    }
}

@Composable fun Setup(mode: Int, onStart: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var li by remember { mutableIntStateOf(0) }
    var ci by remember { mutableIntStateOf(-1) }
    Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(if (mode == 0) "CAREER MANAGER" else "CAREER COACH", fontSize = 22.sp, fontWeight = FontWeight.Black, color = GR)
            Spacer(Modifier.height(8.dp))
            TextField(name, { name = it }, label = { Text("Nama kamu") }, singleLine = true)
            Spacer(Modifier.height(10.dp))
            if (mode == 1) Panel { Text("Mulai dari nol: ambil Lisensi Euro D sampai Pro, lalu latih klub kecil. Kalau performa naik, klub besar akan datang.", fontSize = 13.sp) }
            else LazyColumn {
                items(G.leagues.size) { i ->
                    Text("${G.leagues[i].country} • ${G.leagues[i].name}", fontSize = 13.sp, color = if (i == li) Color.Black else Color.White,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(10.dp)).background(if (i == li) GR else CARD).clickable { li = i; ci = -1 }.padding(10.dp))
                }
            }
        }
        if (mode == 0) Column(Modifier.weight(1f)) {
            LazyColumn {
                items(G.clubs.filter { it.lg == li }) { c ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(10.dp)).background(if (c.id == ci) GR.copy(alpha = .25f) else CARD).clickable { ci = c.id }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Badge(c.name, 34.dp); Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) { Text(c.name, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text("Budget ${eur(c.budget)}", fontSize = 11.sp, color = MUTED) }
                    }
                }
            }
        }
        Column(Modifier.weight(0.6f)) {
            Button(onClick = { G.newGame(mode, name, if (mode == 0) ci else -1); onStart() }, enabled = mode == 1 || ci >= 0) { Text("MULAI ▶") }
        }
    }
}

@Composable fun Nav(icon: String, t: String, sel: Boolean, f: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (sel) GR.copy(alpha = .18f) else Color.Transparent).clickable { f() }.padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 16.sp); Spacer(Modifier.width(8.dp))
        Text(t, fontSize = 13.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) GR else Color.White)
    }
}

@Composable fun Hub(v: Int, onPlay: () -> Unit, onMenu: () -> Unit) {
    var tab by remember { mutableStateOf("Berita") }
    val m = G.clubs.getOrNull(G.me)
    val tabs = if (m == null) listOf("Berita") else listOf("Berita", "Skuad", "Transfer", "Tabel", "Main") + (if (G.mode == 1) listOf("Tawaran") else emptyList())
    val icons = mapOf("Berita" to "📰", "Skuad" to "👥", "Transfer" to "💸", "Tabel" to "🏆", "Main" to "⚽", "Tawaran" to "📩")
    val cur = if (m == null || tab !in tabs) "Berita" else tab
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.width(132.dp).fillMaxHeight().background(Color(0xFF0A1626)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("OTAFM", fontWeight = FontWeight.Black, color = GR, letterSpacing = 3.sp, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            tabs.forEach { t -> Nav(icons[t] ?: "", t, t == cur) { tab = t } }
            Spacer(Modifier.weight(1f))
            if (m != null) OutlinedButton({ G.save() }, Modifier.fillMaxWidth()) { Text("💾 Simpan", fontSize = 12.sp) }
            OutlinedButton(onMenu, Modifier.fillMaxWidth()) { Text("Menu", fontSize = 12.sp) }
        }
        Column(Modifier.weight(1f).fillMaxHeight().padding(12.dp)) {
            Panel(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (m != null) Badge(m.name, 46.dp) else Box(Modifier.size(46.dp).background(GOLD.copy(alpha = .2f), CircleShape), contentAlignment = Alignment.Center) { Text("🧢", fontSize = 22.sp) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (m == null) G.coach else m.name, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(if (m == null) "Calon pelatih • Reputasi ${G.crep}" else "${G.leagues[m.lg].name} • ${G.leagues[m.lg].country}", fontSize = 12.sp, color = MUTED)
                    }
                    if (m != null) {
                        Chip(eur(m.budget), GOLD); Spacer(Modifier.width(6.dp))
                        Chip("Musim ${G.season}/${(G.season + 1) % 100}", Color(0xFF8FB3FF)); Spacer(Modifier.width(6.dp))
                        Chip("Pekan ${G.week + 1}/22", GR)
                    } else Chip("Lisensi ${G.lic}/5", GOLD)
                }
            }
            Spacer(Modifier.height(8.dp))
            when (cur) {
                "Berita" -> Home(v, m)
                "Skuad" -> Squad(v, m!!)
                "Transfer" -> Transfer(v, m!!)
                "Tabel" -> Table(v, m!!)
                "Main" -> PlayTab(v, m!!, onPlay)
                else -> Offers(v)
            }
        }
    }
}

@Composable fun Offers(v: Int) {
    if (G.offers.isEmpty()) Text("Belum ada tawaran klub.", fontSize = 13.sp, color = MUTED)
    G.offers.toList().forEach { id ->
        val c = G.clubs[id]
        Panel(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge(c.name, 40.dp); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text(c.name, fontWeight = FontWeight.Bold); Text("${G.leagues[c.lg].name} • Budget ${eur(c.budget)}", fontSize = 11.sp, color = MUTED) }
                Button({ G.accept(id) }) { Text("Terima") }
            }
        }
    }
}

@Composable fun NewsRow(s: String) {
    val i = s.indexOf(": ")
    val tag = if (i > 0) s.take(i) else "INFO"; val msg = if (i > 0) s.drop(i + 2) else s
    val c = when {
        tag.startsWith("DONE") -> GOLD; tag == "HASIL" -> GR; tag == "TAWARAN" -> Color(0xFFB388FF)
        tag.startsWith("MUSIM") || tag.startsWith("JUARA") -> Color(0xFFFF9248); tag == "LULUS" -> Color(0xFF4DD0E1)
        else -> Color(0xFF8FB3FF)
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp).background(CARD, RoundedCornerShape(12.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(34.dp).background(c, RoundedCornerShape(2.dp))); Spacer(Modifier.width(10.dp))
        Column { Text(tag, color = c, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp); Text(msg, fontSize = 13.sp) }
    }
}

@Composable fun Home(v: Int, m: C?) {
    Column(Modifier.fillMaxSize()) {
        if (m == null) {
            Panel(Modifier.fillMaxWidth()) {
                Text("JALUR LISENSI", fontSize = 11.sp, color = MUTED, letterSpacing = 2.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { G.licN.forEachIndexed { i, n -> Chip(n, if (i < G.lic) GR else if (i == G.lic) GOLD else Color.Gray) } }
                Spacer(Modifier.height(8.dp))
                if (G.lic < 5) Button({ G.course() }) { Text("🎓 Ikut Kursus Lisensi ${G.licN[G.lic]}") }
                else { Text("Tawaran klub untukmu:", fontWeight = FontWeight.Bold); Offers(v) }
            }
            Spacer(Modifier.height(8.dp))
        }
        LazyColumn { items(G.news.size) { NewsRow(G.news[it]) } }
    }
}

@Composable fun Squad(v: Int, m: C) {
    var sell by remember { mutableStateOf<Pair<P, Pair<C, Long>>?>(null) }
    val order = listOf("GK", "DEF", "MID", "FWD")
    LazyColumn {
        items(m.ps.sortedWith(compareBy<P> { order.indexOf(it.pos) }.thenByDescending { it.ovr })) { p ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp).background(CARD, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(42.dp), contentAlignment = Alignment.Center) { Chip(p.pos, posColor(p.pos)) }
                Spacer(Modifier.width(8.dp))
                Text(p.name, Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("${p.age}th", Modifier.width(40.dp), fontSize = 12.sp, color = MUTED)
                Box(Modifier.size(30.dp).background(ovrColor(p.ovr), CircleShape), contentAlignment = Alignment.Center) { Text("${p.ovr}", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp) }
                Text(eur(worth(p)), Modifier.width(76.dp), fontSize = 12.sp, textAlign = TextAlign.End, color = GOLD)
                TextButton({ G.bid(p)?.let { sell = p to it } }) { Text("Jual", fontSize = 12.sp) }
            }
        }
    }
    sell?.let { (p, b) ->
        AlertDialog(onDismissRequest = { sell = null }, containerColor = CARD, title = { Text("Tawaran untuk ${p.name}") },
            text = { Row(verticalAlignment = Alignment.CenterVertically) { Badge(b.first.name, 36.dp); Spacer(Modifier.width(8.dp)); Text("${b.first.name} menawar ${eur(b.second)}") } },
            confirmButton = { TextButton({ G.sell(p, b.first, b.second); sell = null }) { Text("Terima") } },
            dismissButton = { TextButton({ sell = null }) { Text("Tolak") } })
    }
}

@Composable fun Transfer(v: Int, m: C) {
    var pos by remember { mutableStateOf("ALL") }
    var sel by remember { mutableStateOf<Pair<P, C>?>(null) }
    val list = remember(v, pos) { G.market(pos) }
    Column(Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ALL", "GK", "DEF", "MID", "FWD").forEach { x ->
                Text(x, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (x == pos) Color.Black else Color.White,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(if (x == pos) GR else CARD).clickable { pos = x }.padding(horizontal = 14.dp, vertical = 6.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        LazyColumn {
            items(list) { (p, c) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp).background(CARD, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(42.dp), contentAlignment = Alignment.Center) { Chip(p.pos, posColor(p.pos)) }
                    Spacer(Modifier.width(6.dp))
                    Text(p.name, Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Badge(c.name, 24.dp); Spacer(Modifier.width(4.dp))
                    Text(c.name, Modifier.weight(1f), fontSize = 11.sp, color = MUTED)
                    Box(Modifier.size(30.dp).background(ovrColor(p.ovr), CircleShape), contentAlignment = Alignment.Center) { Text("${p.ovr}", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp) }
                    Text(eur(G.ask(p)), Modifier.width(76.dp), fontSize = 12.sp, textAlign = TextAlign.End, color = GOLD)
                    TextButton({ sel = p to c }) { Text("Nego", fontSize = 12.sp) }
                }
            }
        }
    }
    sel?.let { (p, c) -> Neg(p, c) { sel = null } }
}

@Composable fun Neg(p: P, c: C, onClose: () -> Unit) {
    var ask by remember { mutableLongStateOf(G.ask(p)) }
    var offer by remember { mutableLongStateOf((G.ask(p) * 0.8).toLong()) }
    var msg by remember { mutableStateOf("Klub meminta ${eur(G.ask(p))}") }
    AlertDialog(onDismissRequest = onClose, containerColor = CARD, title = { Text("${p.name} (${p.ovr}) - ${c.name}") },
        text = {
            Column {
                Text(msg, color = GOLD)
                Spacer(Modifier.height(6.dp))
                Text("Tawaranmu: ${eur(offer)}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Budget: ${eur(G.clubs[G.me].budget)}", fontSize = 12.sp, color = MUTED)
                Row { TextButton({ offer = (offer * 0.95).toLong() }) { Text("-5%") }; TextButton({ offer = (offer * 1.05).toLong() }) { Text("+5%") } }
            }
        },
        confirmButton = {
            TextButton({
                when (G.reply(offer, ask)) {
                    2 -> { if (G.buy(p, c, offer)) onClose() else { msg = "Budget kurang atau skuad penuh" } }
                    1 -> { ask = (offer + ask) / 2; msg = "Klub menolak, counter: ${eur(ask)}" }
                    else -> { msg = "Tawaran terlalu rendah, ditolak." }
                }
            }) { Text("Tawar") }
        },
        dismissButton = { TextButton(onClose) { Text("Batal") } })
}

@Composable fun Table(v: Int, m: C) {
    var li by remember { mutableIntStateOf(m.lg) }
    val t = G.tbl(li)
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton({ li = (li + G.leagues.size - 1) % G.leagues.size }) { Text("◀") }
            Text("${G.leagues[li].country} • ${G.leagues[li].name}", fontWeight = FontWeight.Black, color = GOLD)
            TextButton({ li = (li + 1) % G.leagues.size }) { Text("▶") }
        }
        LazyColumn {
            itemsIndexed(t) { i, c ->
                Row(Modifier.fillMaxWidth().padding(vertical = 1.dp).background(if (c.id == G.me) GR.copy(alpha = .16f) else if (i % 2 == 0) CARD else Color.Transparent, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}", Modifier.width(26.dp), fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (i < 4) GR else if (i >= t.size - 2) RED else Color.White)
                    Badge(c.name, 24.dp); Spacer(Modifier.width(8.dp))
                    Text(c.name, Modifier.weight(1f), fontSize = 13.sp, color = if (c.id == G.me) GR else Color.White)
                    Text("${c.w}-${c.d}-${c.l}", Modifier.width(70.dp), fontSize = 12.sp, color = MUTED)
                    Text("${c.gf - c.ga}", Modifier.width(36.dp), fontSize = 12.sp)
                    Text("${c.pts}", Modifier.width(34.dp), fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable fun PlayTab(v: Int, m: C, onPlay: () -> Unit) {
    val f = G.fix[G.week].first { it.first == G.me || it.second == G.me }
    val hc = G.clubs[f.first]; val ac = G.clubs[f.second]
    Column(Modifier.fillMaxWidth()) {
        Panel(Modifier.fillMaxWidth()) {
            Text("PEKAN ${G.week + 1}", fontSize = 11.sp, color = MUTED, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Badge(hc.name, 64.dp); Text(hc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("PWR ${G.ms(hc).roundToInt()}", color = GOLD, fontSize = 12.sp) }
                Text("VS", fontSize = 26.sp, fontWeight = FontWeight.Black, color = GR)
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Badge(ac.name, 64.dp); Text(ac.name, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("PWR ${G.ms(ac).roundToInt()}", color = GOLD, fontSize = 12.sp) }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("🛡️ Bertahan", "⚖️ Seimbang", "🔥 Menyerang").forEachIndexed { i, s ->
                Button({ G.ment = i; G.bump() }, colors = ButtonDefaults.buttonColors(containerColor = if (G.ment == i) GR else CARD, contentColor = if (G.ment == i) Color.Black else Color.White)) { Text(s) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onPlay, Modifier.fillMaxWidth().height(50.dp)) { Text("⚽  MULAI PERTANDINGAN", fontWeight = FontWeight.Black, color = Color.Black) }
    }
}

@Composable fun Match(r: Res, onDone: () -> Unit) {
    var min by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (min < 90) { delay(120); if (min < 90) min++ } }
    val h = G.clubs[r.h]; val a = G.clubs[r.a]
    val sh = r.ev.count { it.goal && it.home && it.m <= min }
    val sa = r.ev.count { it.goal && !it.home && it.m <= min }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(Modifier.fillMaxWidth().background(CARD, RoundedCornerShape(16.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Badge(h.name, 44.dp); Spacer(Modifier.width(8.dp))
            Text(h.name, Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text("$sh - $sa", fontSize = 34.sp, fontWeight = FontWeight.Black, color = GR)
            Text(a.name, Modifier.weight(1f), fontWeight = FontWeight.Bold, textAlign = TextAlign.End); Spacer(Modifier.width(8.dp))
            Badge(a.name, 44.dp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$min'", color = GOLD, fontWeight = FontWeight.Black, modifier = Modifier.width(44.dp))
            LinearProgressIndicator(progress = { min / 90f }, Modifier.weight(1f).height(6.dp), color = GR)
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pitch(min, r, Modifier.weight(2f).fillMaxHeight().clip(RoundedCornerShape(14.dp)))
            LazyColumn(Modifier.weight(1f)) {
                items(r.ev.filter { it.m <= min }.reversed()) { e ->
                    Text((if (e.goal) "⚽ " else "🟨 ") + "${e.m}' ${e.t} (${if (e.home) h.name else a.name})", fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).background(CARD, RoundedCornerShape(8.dp)).padding(6.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ min = 90 }) { Text("Lewati") }
            Button(onDone, enabled = min >= 90) { Text("Selesai") }
        }
    }
}

@Composable fun Pitch(min: Int, r: Res, mod: Modifier) {
    val hk = kit(G.clubs[r.h].name).first; val ak = kit(G.clubs[r.a].name).first
    Canvas(mod) {
        val w = size.width; val hh = size.height
        val sw = w / 10f
        for (i in 0 until 10) drawRect(Color(if (i % 2 == 0) 0xFF23863F else 0xFF1E7A39), Offset(i * sw, 0f), Size(sw, hh))
        drawRect(Color.White, Offset(8f, 8f), Size(w - 16, hh - 16), style = Stroke(3f))
        drawLine(Color.White, Offset(w / 2, 8f), Offset(w / 2, hh - 8), 3f)
        drawCircle(Color.White, hh * 0.15f, Offset(w / 2, hh / 2), style = Stroke(3f))
        drawRect(Color.White, Offset(8f, hh * .25f), Size(w * .14f, hh * .5f), style = Stroke(2f))
        drawRect(Color.White, Offset(w - 8f - w * .14f, hh * .25f), Size(w * .14f, hh * .5f), style = Stroke(2f))
        val g = r.ev.lastOrNull { it.goal && it.m <= min && min - it.m < 4 }
        var bx = 0.5f + 0.32f * sin(min * 0.6f)
        val by = 0.5f + 0.3f * sin(min * 1.3f)
        if (g != null) bx = if (g.home) 0.94f else 0.06f
        val hx = listOf(.05f, .2f, .2f, .2f, .2f, .38f, .4f, .38f, .6f, .62f, .6f)
        val hy = listOf(.5f, .15f, .38f, .62f, .85f, .25f, .5f, .75f, .2f, .5f, .8f)
        val sft = (bx - 0.5f) * 0.18f
        for (i in hx.indices) {
            drawCircle(Color.White, 11f, Offset(w * (hx[i] * 0.85f + sft), hh * hy[i]))
            drawCircle(hk, 8f, Offset(w * (hx[i] * 0.85f + sft), hh * hy[i]))
            drawCircle(Color.White, 11f, Offset(w * (1f - hx[i] * 0.85f + sft), hh * hy[i]))
            drawCircle(ak, 8f, Offset(w * (1f - hx[i] * 0.85f + sft), hh * hy[i]))
        }
        drawCircle(Color(0x66FFFFFF), 20f, Offset(w * bx, hh * by))
        drawCircle(Color.White, 11f, Offset(w * bx, hh * by))
    }
}
