package com.growthscore.demo.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.growthscore.demo.R

private data class ReadingBook(val grade:String,val subject:String,val title:String,val author:String,val kind:String,val note:String)
private data class University(val name:String,val city:String,val tags:String,val majors:String,val enrollment:String,val score:String,val gate:Int,val landmark:Int,val imageSource:String)
private data class SchoolDetail(val name:String,val tier:String,val majors:String,val enrollment:String,val score:String)

@Composable
fun ExploreScreen() {
    var section by rememberSaveable { mutableStateOf("书单") }
    var grade by rememberSaveable { mutableStateOf("初一") }
    var subject by rememberSaveable { mutableStateOf("全部") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("探索",fontSize=28.sp,fontWeight=FontWeight.Bold)
        Text("阅读、学科方法与升学参考",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
        Row(Modifier.padding(top=10.dp).horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf("书单","升学参考").forEach { FilterChip(section==it,{section=it},label={Text(it)}) }
        }
        if(section=="书单") ReadingLibrary(grade,subject,{grade=it},{subject=it}) else UniversityLibrary()
    }
}

@Composable private fun ReadingLibrary(grade:String,subject:String,onGrade:(String)->Unit,onSubject:(String)->Unit) {
    val books=readingBooks.filter { it.grade==grade && (subject=="全部" || it.subject==subject) }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(top=12.dp,bottom=20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item {
            Text("分年级学科素养书单",fontSize=20.sp,fontWeight=FontWeight.Bold)
            Text("先完成课标必读，再按兴趣和薄弱能力选择素养拓展。书目为阅读建议，请结合教材版本、教师要求和个人阅读基础选择。",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
            ChoiceRow(listOf("初一","初二","初三","高一","高二","高三"),grade,onGrade)
            ChoiceRow(listOf("全部","语文","数学","英语","理科综合","文科综合"),subject,onSubject)
            Text("${grade}${if(subject=="全部")"全学科" else subject} · ${books.size} 本推荐",fontWeight=FontWeight.Medium,modifier=Modifier.padding(top=4.dp))
        }
        items(books) { BookCard(it) }
        item { ReadingTips() }
    }
}

@Composable private fun ChoiceRow(options:List<String>,selected:String,onSelect:(String)->Unit)=Row(Modifier.fillMaxWidth().padding(top=8.dp).horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){options.forEach{FilterChip(selected==it,{onSelect(it)},label={Text(it)})}}

@Composable private fun BookCard(book:ReadingBook)=Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){ Text(book.title,fontWeight=FontWeight.Bold,fontSize=18.sp); AssistChip(onClick={},label={Text(book.subject)}) }
    Text(book.author,color=Color.Gray,style=MaterialTheme.typography.bodySmall)
    Text(book.note)
    Text(book.kind,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Medium,style=MaterialTheme.typography.bodySmall)
} }

@Composable private fun ReadingTips()=Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
    Text("阅读使用建议",fontWeight=FontWeight.Bold)
    Text("初中建议每月 1–2 本，高中建议每月 2–3 本；可在读完后写下 3 个关键词、1 个问题和 1 段自己的观点。选科后可适当增加对应学科的科普或史学阅读。",style=MaterialTheme.typography.bodySmall)
} }

@Composable private fun UniversityLibrary() {
    var mode by rememberSaveable { mutableStateOf("重点图文") }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(top=12.dp,bottom=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            Text("升学参考",fontSize=20.sp,fontWeight=FontWeight.Bold)
            Text("院校与招生资料整理版。985/211 为历史建设标签，当前建设体系以“双一流”为主；分数、招生人数会随省份、选科、专业和年度变化，不能作为录取预测或志愿填报依据。",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
            ChoiceRow(listOf("重点图文","985 分梯队","特色 211"),mode,{mode=it})
        }
        when(mode) {
            "重点图文" -> { item { Text("重点院校 · 首批图文资料",fontWeight=FontWeight.Bold) }; items(featuredUniversities){ UniversityCard(it) } }
            "985 分梯队" -> { item { Text("39 所 985 · 分梯队详情",fontWeight=FontWeight.Bold); Text("按资料中的梯队整理，保留优势专业、年度本科招生规模和 750 分制分数参考。",style=MaterialTheme.typography.bodySmall,color=Color.Gray) }; items(tiered985){ SchoolDetailCard(it) } }
            else -> { item { Text("代表性特色 211 · 详细资料",fontWeight=FontWeight.Bold); Text("以下为资料中列出的 22 所代表院校；其余地区特色 211 见页面末尾说明。",style=MaterialTheme.typography.bodySmall,color=Color.Gray) }; items(featured211){ SchoolDetailCard(it) }; item { DirectoryCard("其他地区特色 211",listOf("内蒙古大学、广西大学、贵州大学、云南大学、西藏大学、青海大学、宁夏大学、新疆大学、石河子大学、延边大学等，多以地方特色学科见长。","资料参考区间：通常 530–560 分，得分率约 70.7%–74.7%（750 分制）。")) } }
        }
        item { SourceNote() }
    }
}

@Composable private fun UniversityCard(school:University)=Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)) {
    Text(school.name,fontWeight=FontWeight.Bold,fontSize=21.sp)
    Text("${school.city} · ${school.tags}",color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Medium)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) { CampusPhoto(school.gate,"${school.name}校门",Modifier.weight(1f)); CampusPhoto(school.landmark,"${school.name}标志性景观",Modifier.weight(1f)) }
    Text("优势专业：${school.majors}")
    Text("近年本科招生规模：${school.enrollment}")
    Text("近年分数参考：${school.score}",fontWeight=FontWeight.Medium)
    Text(school.imageSource,style=MaterialTheme.typography.labelSmall,color=Color.Gray)
} }

@Composable private fun SchoolDetailCard(school:SchoolDetail)=Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
    Text(school.tier,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Medium,style=MaterialTheme.typography.labelLarge)
    Text(school.name,fontWeight=FontWeight.Bold,fontSize=20.sp)
    Text("优势专业：${school.majors}")
    Text("年招生规模：${school.enrollment}")
    Text("录取分数参考：${school.score}",fontWeight=FontWeight.Medium)
} }

@Composable private fun CampusPhoto(image:Int,description:String,modifier:Modifier) = Image(painter=painterResource(image),contentDescription=description,contentScale=ContentScale.Crop,modifier=modifier.height(118.dp).clip(MaterialTheme.shapes.medium))

@Composable private fun DirectoryCard(title:String,lines:List<String>)=Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) { Text(title,fontSize=19.sp,fontWeight=FontWeight.Bold);lines.forEach{Text(it)} } }
@Composable private fun SourceNote()=Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) { Text("使用说明",fontWeight=FontWeight.Bold);Text("招生规模与分数区间来自用户提供的 2024–2026 年整理资料，统一按 750 分满分折算，仅用于认识院校与专业方向。正式填报请以目标省份教育考试院、招生章程和当年计划为准。",style=MaterialTheme.typography.bodySmall) } }

private val readingBooks=listOf(
    ReadingBook("初一","语文","朝花夕拾","鲁迅","课标必读","短篇叙事与人物情感，适合建立名著阅读批注习惯。"),ReadingBook("初一","语文","西游记","吴承恩","课标必读","训练章回结构理解与古典小说人物分析。"),ReadingBook("初一","数学","给孩子的数学三书","刘薰宇","素养拓展","用生活化语言建立代数、几何与数学思维框架。"),ReadingBook("初一","英语","黑布林英语阅读（初中第 1 级）","分级读物","课内同步","以可理解输入积累词汇和阅读信心。"),ReadingBook("初一","理科综合","趣味物理学","别莱利曼","素养拓展","从生活现象理解力、光与运动。"),ReadingBook("初一","文科综合","中华上下五千年","林汉达","素养拓展","搭建中国历史时间轴。"),
    ReadingBook("初二","语文","红星照耀中国","埃德加·斯诺","课标必读","纪实作品阅读，训练事实、观点和叙述者立场辨析。"),ReadingBook("初二","语文","昆虫记","法布尔","课标必读","观察性文字兼具科学兴趣与细节描写价值。"),ReadingBook("初二","数学","简单的逻辑学（青少版）","D.Q. 麦克伦尼","素养拓展","提升几何证明和应用题的因果推理意识。"),ReadingBook("初二","英语","书虫·牛津英汉双语读物（1–2 级）","牛津大学出版社","分级阅读","以经典故事培养稳定阅读量。"),ReadingBook("初二","理科综合","给孩子讲物理","曹则贤","素养拓展","打通课内概念的底层逻辑。"),ReadingBook("初二","文科综合","给孩子的中国国家地理","中国国家地理","素养拓展","图文结合建立区域与空间认知。"),
    ReadingBook("初三","语文","傅雷家书","傅雷","课标必读","训练书信文本理解，积累真诚而有分寸的表达。"),ReadingBook("初三","语文","文心","夏丏尊、叶圣陶","写作提升","通过故事梳理读写的核心方法。"),ReadingBook("初三","数学","几何原本（少儿彩绘版）","欧几里得","素养拓展","感受公理、定义与证明的严谨链条。"),ReadingBook("初三","英语","新概念英语（1–2 册）","L.G. Alexander","精读训练","用短文精读巩固基础句型与写作表达。"),ReadingBook("初三","理科综合","元素的故事","依·尼查叶夫","素养拓展","从发现史认识元素与化学的联系。"),ReadingBook("初三","文科综合","史记（少年版）","司马迁","素养拓展","积累史料阅读与人物评价视角。"),
    ReadingBook("高一","语文","论语","孔子及弟子","课标必读","在注释辅助下建立文言语感与思想对话能力。"),ReadingBook("高一","语文","乡土中国","费孝通","补充推荐","理解社会结构，为论述类阅读与写作积累概念。"),ReadingBook("高一","数学","什么是数学","R. 柯朗","素养拓展","重新理解函数、结构与数学的本质。"),ReadingBook("高一","英语","书虫·牛津英汉双语读物（3–4 级）","牛津大学出版社","分级阅读","从分级读物过渡到更长的英文篇章。"),ReadingBook("高一","理科综合","从一到无穷大","G. 伽莫夫","素养拓展","连接物理、数学和宇宙学的科学想象。"),ReadingBook("高一","文科综合","全球通史","斯塔夫里阿诺斯","素养拓展","建立世界史的整体关联。"),
    ReadingBook("高二","语文","红楼梦","曹雪芹","课标必读","适合主题、人物关系和叙事细节的深度阅读。"),ReadingBook("高二","语文","古文观止（评注版）","吴楚材、吴调侯编","鉴赏提升","通过篇目精读积累文言实词、句式与表达。"),ReadingBook("高二","数学","数学与猜想","G. 波利亚","素养拓展","区分合情推理与论证推理，训练探究意识。"),ReadingBook("高二","英语","新概念英语（第 3 册）","L.G. Alexander","精读训练","学习复杂句、段落衔接和正式表达。"),ReadingBook("高二","理科综合","视觉之旅：神奇的化学元素","西奥多·格雷","素养拓展","用视觉线索深化元素性质与材料认知。"),ReadingBook("高二","文科综合","中国历代政治得失","钱穆","素养拓展","以制度视角理解历史叙述。"),
    ReadingBook("高三","语文","谈美书简","朱光潜","课标必读","在冲刺阶段保持审美与思辨阅读。"),ReadingBook("高三","语文","作文三书","王鼎钧","写作提升","针对议论、叙事和语言表达进行方法复盘。"),ReadingBook("高三","数学","证明与反驳","伊姆雷·拉卡托斯","素养拓展","理解数学发现过程，拓宽证明思维。"),ReadingBook("高三","英语","小王子（英文原版）","Antoine de Saint-Exupéry","原版入门","短篇原著适合保持语感并积累表达。"),ReadingBook("高三","理科综合","细胞生命的礼赞","刘易斯·托马斯","素养拓展","建立生命科学的整体观。"),ReadingBook("高三","文科综合","法治的细节","罗翔","素养拓展","以案例理解法治与论证结构。")
)

private val featuredUniversities=listOf(
    University("清华大学","北京","985 · 211 · 双一流","力学、机械、电气、计算机、建筑、核科学等","约 3700 人（含强基、专项等）","690–705 分（约 92%–94%，750 分制参考）",R.drawable.thu_gate,R.drawable.thu_campus,"校园图片：Wikimedia Commons · TsinghuaUniversityGate.JPG、TsinghuaUniversitypic2.jpg。"),
    University("北京大学","北京","985 · 211 · 双一流","哲学、法学、中文、数学、物理、化学、生物、医学等","约 3800 人","688–703 分（约 91.7%–93.7%，750 分制参考）",R.drawable.pku_gate,R.drawable.pku_library,"校园图片：Wikimedia Commons · West Gate of Peking University original.JPG、北京大学图书馆及图书馆前的石狮子.jpg。"),
    University("上海交通大学","上海","985 · 211 · 双一流","机械、船海、临床医学、生物、计算机、动力工程等","约 4500 人","670–685 分（约 89.3%–91.3%，750 分制参考）",R.drawable.sjtu_gate,R.drawable.sjtu_campus,"校园图片：Wikimedia Commons · Shanghai Jiao Tong University Gate.jpg、SJTU Xuhui Campus Gate.JPG。"),
    University("复旦大学","上海","985 · 211 · 双一流","经济、新闻传播、数学、物理、基础医学、临床医学等","约 3900 人","668–682 分（约 89.1%–90.9%，750 分制参考）",R.drawable.fudan_gate,R.drawable.fudan_campus,"校园图片：Wikimedia Commons · 201704 Gate of Fudan University Jiangwan Campus.jpg、Fudan building1.jpg。")
)

private val tiered985=listOf(
    SchoolDetail("清华大学","第一梯队 · 顶尖学府","力学、机械、仪器、材料、动力、电气、控制、计算机、建筑、核科学等 21 个 A+ 学科，工科整体实力突出。","约 3700 人（含强基、专项等）","690–705 分，得分率约 92%–94%"),
    SchoolDetail("北京大学","第一梯队 · 顶尖学府","哲学、理论经济、法学、政治学、中文、考古、数学、物理、化学、生物、基础医学、口腔医学等 25 个 A+ 学科，文理医综合顶尖。","约 3800 人","688–703 分，得分率约 91.7%–93.7%"),
    SchoolDetail("上海交通大学","第二梯队 · 华东五校 + 人大","机械、船舶与海洋、临床医学、生物、计算机、动力工程等 A+ 学科，工科与医学双强。","约 4500 人","670–685 分，得分率约 89.3%–91.3%"),
    SchoolDetail("复旦大学","第二梯队 · 华东五校 + 人大","理论经济、新闻传播、数学、物理、基础医学、临床医学、哲学、中文等 A+ 学科，文理医顶尖。","约 3900 人","668–682 分，得分率约 89.1%–90.9%"),
    SchoolDetail("中国科学技术大学","第二梯队 · 华东五校 + 人大","物理、化学、天文、地球物理、核科学、安全科学等 A+ 学科，理科实力突出，精英化培养。","约 1900 人","665–680 分，得分率约 88.7%–90.7%"),
    SchoolDetail("南京大学","第二梯队 · 华东五校 + 人大","天文、地质、图情档案、哲学、中文、物理、化学等 A+ 学科，文理均衡。","约 3500 人","660–675 分，得分率约 88%–90%"),
    SchoolDetail("浙江大学","第二梯队 · 华东五校 + 人大","生态、光学工程、控制、计算机、农业工程、软件工程等 A+ 学科，学科门类全面。","约 6300 人","660–676 分，得分率约 88%–90.1%"),
    SchoolDetail("中国人民大学","第二梯队 · 华东五校 + 人大","理论经济、应用经济、法学、社会学、新闻传播、统计、工商管理、公共管理等 A+ 学科，人文社科优势突出。","约 2800 人","658–673 分，得分率约 87.7%–89.7%"),
    SchoolDetail("北京航空航天大学","第三梯队 · 中坚 985","航空宇航、仪器、材料、控制等 A+ 学科，航空航天特色鲜明。","约 3900 人","650–668 分，得分率约 86.7%–89.1%"),
    SchoolDetail("北京师范大学","第三梯队 · 中坚 985","教育学、心理学、中文、中国史、地理、戏剧与影视等 A+ 学科，师范类优势突出。","约 2500 人（北京校区）","640–660 分，得分率约 85.3%–88%"),
    SchoolDetail("哈尔滨工业大学","第三梯队 · 中坚 985","力学、机械、控制、环境、航空宇航等 A+ 学科，一校三区，工科底蕴深厚。","约 7000 人（本部 + 威海 + 深圳）","645–665 分，得分率约 86%–88.7%"),
    SchoolDetail("北京理工大学","第三梯队 · 中坚 985","兵器、机械、控制、光学工程等 A+ 学科，国防军工特色鲜明。","约 3700 人","642–660 分，得分率约 85.6%–88%"),
    SchoolDetail("西安交通大学","第三梯队 · 中坚 985","动力、电气、核科学、机械、管理科学等 A+ 学科，西部工科龙头。","约 4800 人","638–655 分，得分率约 85.1%–87.3%"),
    SchoolDetail("电子科技大学","第三梯队 · 中坚 985","电子科学、信息与通信、计算机等 A+ 学科，电子信息领域优势突出。","约 5000 人","620–645 分，得分率约 82.7%–86%"),
    SchoolDetail("同济大学","第三梯队 · 中坚 985","土木、环境、城乡规划、建筑等 A+ 学科，建筑土木特色突出。","约 4500 人","635–652 分，得分率约 84.7%–86.9%"),
    SchoolDetail("华中科技大学","第三梯队 · 中坚 985","机械、光学、生物医学、公共卫生与预防医学等 A+ 学科，工科与医学双强。","约 7100 人","633–650 分，得分率约 84.4%–86.7%"),
    SchoolDetail("武汉大学","第三梯队 · 中坚 985","马克思主义理论、地球物理、测绘、图情档案、法学等 A+ 学科，文理综合实力强。","约 7200 人","630–648 分，得分率约 84%–86.4%"),
    SchoolDetail("东南大学","第三梯队 · 中坚 985","建筑、土木、交通运输、生物医学工程等 A+ 学科，建筑交通见长。","约 4100 人","628–645 分，得分率约 83.7%–86%"),
    SchoolDetail("西北工业大学","第三梯队 · 中坚 985","航空宇航、材料、计算机等 A 类学科，航空、航天、航海特色鲜明。","约 4700 人（资料称 2026 年扩招至 4720 人）","625–642 分，得分率约 83.3%–85.6%"),
    SchoolDetail("南开大学","第三梯队 · 中坚 985","理论经济、数学、化学、统计、世界史等 A 类学科，文理老牌名校。","约 4100 人","623–640 分，得分率约 83.1%–85.3%"),
    SchoolDetail("天津大学","第四梯队 · 地域与特色 985","化学工程、机械、光学工程、建筑等 A 类学科，工科传统强。","约 5000 人","620–638 分，得分率约 82.7%–85.1%"),
    SchoolDetail("中山大学","第四梯队 · 地域与特色 985","生态、工商管理、哲学、临床医学等 A 类学科，华南综合优势突出。","约 7700 人","618–635 分，得分率约 82.4%–84.7%"),
    SchoolDetail("厦门大学","第四梯队 · 地域与特色 985","海洋科学、应用经济、化学、统计等 A 类学科，财经与理科特色突出。","约 5300 人","615–632 分，得分率约 82%–84.3%"),
    SchoolDetail("四川大学","第四梯队 · 地域与特色 985","口腔医学、临床医学、护理学、中文等 A+ 学科，医学优势突出，招生规模较大。","约 9200 人","608–628 分，得分率约 81.1%–83.7%"),
    SchoolDetail("华南理工大学","第四梯队 · 地域与特色 985","轻工、食品、材料等 A+ 学科，华南工科优势突出。","约 6300 人","605–625 分，得分率约 80.7%–83.3%"),
    SchoolDetail("大连理工大学","第四梯队 · 地域与特色 985","力学、化工、机械、管理科学等 A 类学科，东北工科核心。","约 6300 人（主校区 + 盘锦校区）","600–620 分，得分率约 80%–82.7%"),
    SchoolDetail("吉林大学","第四梯队 · 地域与特色 985","化学、法学、考古、机械等 A 类学科，学科门类齐全，招生规模大。","约 10300 人","595–615 分，得分率约 79.3%–82%"),
    SchoolDetail("山东大学","第四梯队 · 地域与特色 985","数学、中文、临床医学等 A 类学科，一校三地。","约 10500 人（资料称 2026 年 10475 人）","595–618 分，得分率约 79.3%–82.4%"),
    SchoolDetail("重庆大学","第四梯队 · 地域与特色 985","机械、仪器、土木等 A 类学科，西南工科强校。","约 6300 人","590–610 分，得分率约 78.7%–81.3%"),
    SchoolDetail("中南大学","第四梯队 · 地域与特色 985","冶金、矿业、护理、临床医学等 A+ 学科，医学与材料优势突出。","约 8400 人","592–612 分，得分率约 78.9%–81.6%"),
    SchoolDetail("湖南大学","第四梯队 · 地域与特色 985","机械、土木、化学工程等 A 类学科，工科见长。","约 5300 人","588–608 分，得分率约 78.4%–81.1%"),
    SchoolDetail("东北大学","第四梯队 · 地域与特色 985","控制、材料、计算机等 A 类学科，自动化领域优势突出。","约 5300 人（资料称 2026 年 5334 人）","585–605 分，得分率约 78%–80.7%"),
    SchoolDetail("中国农业大学","第四梯队 · 地域与特色 985","作物、畜牧、农业工程、食品、植物保护等 6 个 A+ 学科，农林特色突出。","约 4200 人（资料含 2025 年 4185 人）","580–600 分，得分率约 77.3%–80%"),
    SchoolDetail("中国海洋大学","第四梯队 · 地域与特色 985","海洋科学、水产等 A+ 学科，海洋领域特色鲜明。","约 4100 人","578–598 分，得分率约 77.1%–79.7%"),
    SchoolDetail("中央民族大学","第四梯队 · 地域与特色 985","民族学、少数民族语言文学等 A+ 学科，民族类院校特色突出。","约 2800 人","570–590 分，得分率约 76%–78.7%"),
    SchoolDetail("西北农林科技大学","第四梯队 · 地域与特色 985","农学、林学、植物保护、水土保持与荒漠化防治等 A+ 学科，农林水特色鲜明。","约 5500 人","565–585 分，得分率约 75.3%–78%"),
    SchoolDetail("兰州大学","第四梯队 · 地域与特色 985","化学、大气科学、生态、草学等 A+ 学科，西北综合型大学。","约 4800 人","560–580 分，得分率约 74.7%–77.3%"),
    SchoolDetail("华东师范大学","第四梯队 · 地域与特色 985","教育、生态、统计等 A+ 学科，师范教育优势突出。","约 3500 人","610–630 分，得分率约 81.3%–84%"),
    SchoolDetail("国防科技大学","第四梯队 · 地域与特色 985","系统科学、计算机、航空宇航、软件工程等 A+ 学科，军队院校，需军检政审。","约 1500 人（生长军官学员）","630–660 分，得分率约 84%–88%，省份与性别差异较大")
)

private val featured211=listOf(
    SchoolDetail("中央财经大学","头部特色 211","应用经济、工商管理、统计等 A+ 学科，财经领域特色突出。","约 2500 人","620–640 分，得分率约 82.7%–85.3%"),
    SchoolDetail("上海财经大学","头部特色 211","应用经济、工商管理等 A 类学科，财经特色鲜明。","约 2000 人","625–645 分，得分率约 83.3%–86%"),
    SchoolDetail("对外经济贸易大学","头部特色 211","应用经济、法学、外国语言文学等 A 类学科，国贸与外语特色。","约 2100 人","615–635 分，得分率约 82%–84.7%"),
    SchoolDetail("北京邮电大学","头部特色 211","信息与通信、计算机、电子科学等 A+ 学科，信息科技特色突出。","约 3100 人","620–645 分，得分率约 82.7%–86%"),
    SchoolDetail("西安电子科技大学","头部特色 211","电子科学、信息与通信等 A+ 学科，“两电一邮”成员。","约 5500 人","600–625 分，得分率约 80%–83.3%"),
    SchoolDetail("中国政法大学","头部特色 211","法学 A+ 学科，政法特色鲜明。","约 2100 人","610–630 分，得分率约 81.3%–84%"),
    SchoolDetail("北京外国语大学","头部特色 211","外国语言文学 A+ 学科，外语类院校代表。","约 1500 人","605–625 分，得分率约 80.7%–83.3%"),
    SchoolDetail("上海外国语大学","头部特色 211","外国语言文学 A 类学科，外语教育优势突出。","约 1500 人","600–620 分，得分率约 80%–82.7%"),
    SchoolDetail("北京交通大学","行业 / 区域 211","系统科学 A+，交通运输、信息通信等 A 类学科，轨道交通特色。","约 4400 人","590–610 分，得分率约 78.7%–81.3%"),
    SchoolDetail("北京科技大学","行业 / 区域 211","冶金、材料、科技史等 A+ 学科，材料冶金特色。","约 3800 人","585–605 分，得分率约 78%–80.7%"),
    SchoolDetail("华北电力大学","行业 / 区域 211","电气工程 A 类学科，电力行业特色。","约 6000 人（北京 + 保定校区）","580–600 分，得分率约 77.3%–80%"),
    SchoolDetail("中国石油大学（北京 / 华东）","行业 / 区域 211","石油与天然气、地质资源与地质工程等 A+ 学科，油气领域特色。","约 4800 人（北京校区约 2360 人）","570–590 分，得分率约 76%–78.7%"),
    SchoolDetail("中国传媒大学","行业 / 区域 211","新闻传播、戏剧与影视等 A+ 学科，传媒领域特色。","约 2500 人","585–605 分，得分率约 78%–80.7%"),
    SchoolDetail("南京航空航天大学","行业 / 区域 211","航空宇航、力学等 A 类学科，航空领域核心院校。","约 4800 人","580–600 分，得分率约 77.3%–80%"),
    SchoolDetail("南京理工大学","行业 / 区域 211","兵器科学与技术 A+ 学科，军工特色。","约 4500 人","575–595 分，得分率约 76.7%–79.3%"),
    SchoolDetail("武汉理工大学","行业 / 区域 211","材料 A+ 学科，建材、交通、汽车行业特色。","约 9000 人","570–590 分，得分率约 76%–78.7%"),
    SchoolDetail("西南交通大学","行业 / 区域 211","交通运输 A+ 学科，轨道交通特色鲜明。","约 7500 人","565–585 分，得分率约 75.3%–78%"),
    SchoolDetail("长安大学","行业 / 区域 211","交通运输、地质资源与地质工程等 A 类学科，公路交通特色。","约 6500 人","555–575 分，得分率约 74%–76.7%"),
    SchoolDetail("苏州大学","地方综合类 211","设计学、软件工程等 A 类学科，区域综合实力较强。","约 6800 人","570–590 分，得分率约 76%–78.7%"),
    SchoolDetail("上海大学","地方综合类 211","社会学、美术学等 A 类学科，上海地方重点高校。","约 5500 人","565–585 分，得分率约 75.3%–78%"),
    SchoolDetail("暨南大学","地方综合类 211","新闻传播、应用经济等 A 类学科，华南侨校特色。","约 4800 人","560–580 分，得分率约 74.7%–77.3%"),
    SchoolDetail("西北大学","地方综合类 211","考古、理论经济等 A 类学科，西北文科特色。","约 3800 人","550–570 分，得分率约 73.3%–76%")
)
