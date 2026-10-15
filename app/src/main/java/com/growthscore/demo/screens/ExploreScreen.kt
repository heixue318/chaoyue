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
    var mode by rememberSaveable { mutableStateOf("重点院校") }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(top=12.dp,bottom=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            Text("升学参考",fontSize=20.sp,fontWeight=FontWeight.Bold)
            Text("院校与招生资料整理版。985/211 为历史建设标签，当前建设体系以“双一流”为主；分数、招生人数会随省份、选科、专业和年度变化，不能作为录取预测或志愿填报依据。",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
            ChoiceRow(listOf("重点院校","985 地区目录","特色 211"),mode,{mode=it})
        }
        when(mode) {
            "重点院校" -> { item { Text("重点院校 · 首批图文资料",fontWeight=FontWeight.Bold) }; items(featuredUniversities){ UniversityCard(it) } }
            "985 地区目录" -> item { DirectoryCard("39 所 985 院校地区目录",listOf("北京：北京大学、清华大学、中国人民大学、北航、北理工、农大、北师大、中央民大","上海：复旦大学、同济大学、上海交通大学、华东师范大学","江苏 / 浙江 / 安徽：南京大学、东南大学、浙江大学、中国科学技术大学","湖北 / 湖南 / 广东：武汉大学、华中科技大学、湖南大学、中南大学、国防科技大学、中山大学、华南理工大学","陕西 / 四川 / 重庆 / 甘肃：西安交大、西北工大、西北农林、四川大学、电子科大、重庆大学、兰州大学","其余地区：南开、天大、大连理工、东北大学、吉林大学、哈工大、厦大、山大、中国海洋、重大等")) }
            else -> item { DirectoryCard("代表性特色 211",listOf("财经与经贸：中央财经大学、上海财经大学、对外经济贸易大学","信息与电子：北京邮电大学、西安电子科技大学","外语与政法：北京外国语大学、上海外国语大学、中国政法大学","交通与工科：北京交通大学、华北电力大学、南京航空航天大学、武汉理工大学、西南交通大学、长安大学","区域综合：苏州大学、上海大学、暨南大学、西北大学，以及各省区特色高校")) }
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
