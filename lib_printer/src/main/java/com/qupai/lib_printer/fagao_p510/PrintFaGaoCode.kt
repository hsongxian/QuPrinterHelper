package com.qupai.lib_printer.fagao_p510

object PrintFaGaoCode {
    // MOTION-PART
    const val S51PS_M_CARDIN:Long = 0x0000000000000001    // inserting card  进卡
    const val S51PS_M_CARDMOVE:Long = 0x0000000000000002    // moving card 移动卡片
    const val S51PS_M_CARDMOVEEXT:Long = 0x0000000000000004    // moving card between external 2个模块之间移动卡片
    const val S51PS_M_CARDEJECT:Long = 0x0000000000000008    // ejecting card  排除卡片
    const val S51PS_M_THEADLIFT:Long = 0x0000000000000010    // lifting up/down thermal head  打印头抬起落下
    const val S51PS_M_ICLIFT:Long = 0x0000000000000020    // lifting up/down ic connector  IC读头抬起落下
    const val S51PS_M_RIBBONSEARCH:Long = 0x0000000000000040    // searching ribbon              查找色带
    const val S51PS_M_RIBBONWIND:Long = 0x0000000000000080    // winding ribbon
    const val S51PS_M_MAGNETIC:Long = 0x0000000000000100    // processing magnetic      处理磁条卡
    const val S51PS_M_PRINT:Long = 0x0000000000000200    // printing    打印
    const val S51PS_M_INIT:Long = 0x0000000000000400    // initializing  初始化
    const val S51PS_S_CONNHOPPER:Long = 0x0000000000000800    // hopper connected 已安装进卡盒
    const val S51PS_S_CONNICENCODEER:Long = 0x0000000000001000    // ic encoder connected     已安装接触IC
    const val S51PS_S_CONNMAGNETIC:Long = 0x0000000000002000    // magnetic encoder connected  已安装写磁模块
    const val S51PS_S_CONNLAMINATOR:Long = 0x0000000000004000    // laminator connected 已经安装覆膜机
    const val S51PS_S_CONNFLIPPER:Long = 0x0000000000008000    // flipper connected 已经安装双面翻转模块
    const val S51PS_S_FLIPPERTOP:Long = 0x0000000000010000    // flipper is top sided  翻转模块是在顶部
    const val S51PS_S_COVEROPENED:Long = 0x0000000000020000    // cover is opened  上盖已打卡
    const val S51PS_S_DETECTIN:Long = 0x0000000000040000    // detect a card from in sensor  在进卡传感器位置检测到卡片
    const val S51PS_S_DETECTOUT:Long = 0x0000000000080000    // detect a card from out sensor  在出卡传感器位置检测到卡片
    const val S51PS_S_CARDEMPTY:Long = 0x0000000000100000    // card empty              卡盒无卡
    const val S51PS_S_RECVPRINTDATA:Long = 0x0000000000200000    // receiving print data    接受打印数据
    const val S51PS_S_HAVEPRINTDATA:Long = 0x0000000000400000    // having print data    已经有打印数据
    const val S51PS_S_NEEDCLEANING:Long = 0x0000000004000000    // need cleaning   需要清洁
    const val S51PS_S_SWLOCKED:Long = 0x0000000008000000    // system locked (sw) 系统锁死
    const val S51PS_S_HWLOCKED:Long = 0x0000000010000000    // system locked (hw) 系统锁死
    const val S51PS_M_SBSCOMMAND:Long = 0x0000000020000000    // doing SBS command  执行SBS命令
    const val S51PS_S_SBSMODE:Long = 0x0000000040000000    // under SBS mode   当前是SBS模式
    const val S51PS_S_TESTMODE:Long = 0x0000000080000000    // test mode   测试模块
}