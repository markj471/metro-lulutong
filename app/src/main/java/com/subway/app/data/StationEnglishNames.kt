package com.subway.app.data

/** Official-style English labels for common Wuhan station names. */
object StationEnglishNames {
    private val exact = mapOf(
        "天河机场" to "Tianhe Airport",
        "航空总部" to "Aviation Headquarters",
        "汉口火车站" to "Hankou Railway Station",
        "武汉火车站" to "Wuhan Railway Station",
        "武汉站" to "Wuhan Railway Station",
        "武汉站东广场" to "Wuhan Railway Station East Square",
        "武汉站西广场" to "Wuhan Railway Station West Square",
        "光谷广场" to "Optics Valley Square",
        "洪山广场" to "Hongshan Square",
        "中山公园" to "Zhongshan Park",
        "码头潭公园" to "Matoutan Park",
        "墨水湖公园" to "Moshui Lake Park",
        "江汉路" to "Jianghan Road",
        "利济北路" to "Liji North Road",
        "光谷五路" to "Optics Valley 5th Road",
        "光谷六路" to "Optics Valley 6th Road",
        "光谷七路" to "Optics Valley 7th Road",
        "光谷八路" to "Optics Valley 8th Road",
        "光谷九路" to "Optics Valley 9th Road",
        "武汉大学" to "Wuhan University",
        "华中科技大学" to "Huazhong University of Science and Technology",
        "湖北大学" to "Hubei University",
        "江夏客厅" to "Jiangxia Civic Center",
        "军运村" to "Military Games Village",
        "葛店南站" to "Gedian South Railway Station",
        "佛祖岭" to "Fozuling",
        "径河" to "Jinghe",
        "汉口北" to "Hankou North",
        "街道口" to "Jiedaokou",
        "循礼门" to "Xunlimen",
        "王家墩东" to "Wangjiadun East",
        "王家墩东站" to "Wangjiadun East",
        "常青城" to "Changqingcheng",
        "常青花园" to "Changqing Garden",
        "金银潭" to "Jinyin Lake",
        "黄浦路" to "Huangpu Road",
        "三阳路" to "Sanyang Road",
        "大智路" to "Dazhi Road",
        "宗关" to "Zongguan",
        "新荣" to "Xinrong",
        "阳逻开发区" to "Yangluo Development Zone",
        "新城十一路" to "Xincheng 11th Road"
    )

    private val suffixes = listOf(
        Triple("地铁小镇", "di tie xiao zhen", "Metro Town"),
        Triple("开发区", "kai fa qu", "Development Zone"),
        Triple("火车站", "huo che zhan", "Railway Station"),
        Triple("机场", "ji chang", "Airport"),
        Triple("广场", "guang chang", "Square"),
        Triple("公园", "gong yuan", "Park"),
        Triple("大道", "da dao", "Avenue"),
        Triple("大街", "da jie", "Street"),
        Triple("路", "lu", "Road"),
        Triple("湖", "hu", "Lake"),
        Triple("山", "shan", "Mountain")
    )

    fun forStation(chineseName: String, pinyinFallback: String): String {
        exact[chineseName]?.let { return it }
        val suffix = suffixes.firstOrNull { chineseName.endsWith(it.first) }
        if (suffix != null) {
            val romanized = titleCase(pinyinFallback.removeSuffix(suffix.second).trim())
            return listOf(romanized, suffix.third).filter { it.isNotBlank() }.joinToString(" ")
        }
        return titleCase(pinyinFallback)
    }

    private fun titleCase(value: String): String = value.trim().split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { token -> token.lowercase().replaceFirstChar { it.titlecase() } }
}
