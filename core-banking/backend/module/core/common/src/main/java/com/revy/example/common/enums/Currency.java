package com.revy.example.common.enums;

/**
 * ISO 4217 통화 코드 enum.
 *
 * <p>200+ 항목으로 셀렉트박스에 그대로 노출하기엔 부적합하므로
 * {@code ExposedEnum}을 구현하지 않습니다. 프론트엔드에서는 별도의 통화 검색 컴포넌트를 사용하세요.
 */
public enum Currency implements ExposedEnum {
    // --- A ---
    ADP("ADP", "020", "ADP", 0),
    AED("AED", "784", "د.إ", 2),
    AFA("AFA", "004", "؋", 2),
    AFN("AFN", "971", "؋", 2),
    ALL("ALL", "008", "L", 2),
    AMD("AMD", "051", "֏", 2),
    ANG("ANG", "532", "ƒ", 2),
    AOA("AOA", "973", "Kz", 2),
    ARS("ARS", "032", "$", 2),
    ATS("ATS", "040", "öS", 2),
    AUD("AU$", "036", "$", 2),
    AWG("AWG", "533", "ƒ", 2),
    AYM("AYM", "945", "AYM", 2),
    AZM("AZM", "031", "₼", 2),
    AZN("AZN", "944", "₼", 2),

    // --- B ---
    BAM("BAM", "977", "КМ", 2),
    BBD("BBD", "052", "$", 2),
    BDT("BDT", "050", "৳", 2),
    BEF("BEF", "056", "fr", 0),
    BGL("BGL", "100", "лв", 2),
    BGN("BGN", "975", "лв", 2),
    BHD("BHD", "048", "ب.د", 3),
    BIF("BIF", "108", "₣", 0),
    BMD("BMD", "060", "$", 2),
    BND("BND", "096", "$", 2),
    BOB("BOB", "068", "Bs.", 2),
    BOV("BOV", "984", "Mvdol", 2),
    BRL("R$", "986", "R$", 2),
    BSD("BSD", "044", "$", 2),
    BTN("BTN", "064", "Nu", 2),
    BWP("BWP", "072", "P", 2),
    BYB("BYB", "112", "Br", 0),
    BYN("BYN", "933", "Br", 2),
    BYR("BYR", "974", "Br", 0),
    BZD("BZD", "084", "$", 2),

    // --- C ---
    CAD("CA$", "124", "$", 2),
    CDF("CDF", "976", "₣", 2),
    CHE("CHE", "947", "CHE", 2),
    CHF("CHF", "756", "CHF", 2),
    CHW("CHW", "948", "CHW", 2),
    CLF("CLF", "990", "UF", 4),
    CLP("CLP", "152", "$", 0),
    CNY("CN¥", "156", "¥", 2),
    COP("COP", "170", "$", 2),
    COU("COU", "970", "COU", 2),
    CRC("CRC", "188", "₡", 2),
    CSD("CSD", "891", "din", 2),
    CUC("CUC", "931", "$", 2),
    CUP("CUP", "192", "₱", 2),
    CVE("CVE", "132", "$", 2),
    CYP("CYP", "196", "£", 2),
    CZK("CZK", "203", "Kč", 2),

    // --- D ---
    DEM("DEM", "276", "DM", 2),
    DJF("DJF", "262", "₣", 0),
    DKK("DKK", "208", "kr.", 2),
    DOP("DOP", "214", "$", 2),
    DZD("DZD", "012", "د.ج", 2),

    // --- E ---
    EEK("EEK", "233", "kr", 2),
    EGP("EGP", "818", "£", 2),
    ERN("ERN", "232", "Nfk", 2),
    ESP("ESP", "724", "₧", 0),
    ETB("ETB", "230", "Br", 2),
    EUR("€",   "978", "€", 2),

    // --- F ---
    FIM("FIM", "246", "mk", 2),
    FJD("FJD", "242", "$", 2),
    FKP("FKP", "238", "£", 2),
    FRF("FRF", "250", "fr", 2),

    // --- G ---
    GBP("£",   "826", "£", 2),
    GEL("GEL", "981", "₾", 2),
    GHC("GHC", "288", "₵", 2),
    GHS("GHS", "936", "₵", 2),
    GIP("GIP", "292", "£", 2),
    GMD("GMD", "270", "D", 2),
    GNF("GNF", "324", "₣", 0),
    GRD("GRD", "300", "₯", 0),
    GTQ("GTQ", "320", "Q", 2),
    GWP("GWP", "624", "GWP", 2),
    GYD("GYD", "328", "$", 2),

    // --- H ---
    HKD("HK$", "344", "$", 2),
    HNL("HNL", "340", "L", 2),
    HRK("HRK", "191", "kn", 2),
    HTG("HTG", "332", "G", 2),
    HUF("HUF", "348", "Ft", 2),

    // --- I ---
    IDR("IDR", "360", "Rp", 2),
    IEP("IEP", "372", "£", 2),
    ILS("₪",   "376", "₪", 2),
    INR("₹",   "356", "₹", 2),
    IQD("IQD", "368", "ع.د", 3),
    IRR("IRR", "364", "﷼", 2),
    ISK("ISK", "352", "kr", 0),
    ITL("ITL", "380", "₤", 0),

    // --- J ---
    JMD("JMD", "388", "$", 2),
    JOD("JOD", "400", "د.ا", 3),
    JPY("JP¥", "392", "¥", 0),

    // --- K ---
    KES("KES", "404", "Sh", 2),
    KGS("KGS", "417", "лв", 2),
    KHR("KHR", "116", "៛", 2),
    KMF("KMF", "174", "FC", 0),
    KPW("KPW", "408", "₩", 2),
    KRW("₩",   "410", "₩", 0),
    KWD("KWD", "414", "د.ك", 3),
    KYD("KYD", "136", "$", 2),
    KZT("KZT", "398", "₸", 2),

    // --- L ---
    LAK("LAK", "418", "₭", 2),
    LBP("LBP", "422", "ل.ل", 2),
    LKR("LKR", "144", "₨", 2),
    LRD("LRD", "430", "$", 2),
    LSL("LSL", "426", "L", 2),
    LTL("LTL", "440", "Lt", 2),
    LUF("LUF", "442", "fr", 0),
    LVL("LVL", "428", "Ls", 2),
    LYD("LYD", "434", "ل.د", 3),

    // --- M ---
    MAD("MAD", "504", "د.م.", 2),
    MDL("MDL", "498", "L", 2),
    MGA("MGA", "969", "Ar", 2),
    MGF("MGF", "450", "FMG", 0),
    MKD("MKD", "807", "ден", 2),
    MMK("MMK", "104", "K", 2),
    MNT("MNT", "496", "₮", 2),
    MOP("MOP", "446", "P", 2),
    MRO("MRO", "478", "UM", 2),
    MRU("MRU", "929", "UM", 2),
    MTL("MTL", "470", "₤", 2),
    MUR("MUR", "480", "₨", 2),
    MVR("MVR", "462", "ރ", 2),
    MWK("MWK", "454", "MK", 2),
    MXN("MX$", "484", "$", 2),
    MXV("MXV", "979", "MXV", 2),
    MYR("MYR", "458", "RM", 2),
    MZM("MZM", "508", "MT", 2),
    MZN("MZN", "943", "MT", 2),

    // --- N ---
    NAD("NAD", "516", "$", 2),
    NGN("NGN", "566", "₦", 2),
    NIO("NIO", "558", "C$", 2),
    NLG("NLG", "528", "ƒ", 2),
    NOK("NOK", "578", "kr", 2),
    NPR("NPR", "524", "₨", 2),
    NZD("NZ$", "554", "$", 2),

    // --- O ---
    OMR("OMR", "512", "﷼", 3),

    // --- P ---
    PAB("PAB", "590", "B/.", 2),
    PEN("PEN", "604", "S/.", 2),
    PGK("PGK", "598", "K", 2),
    PHP("PHP", "608", "₱", 2),
    PKR("PKR", "586", "₨", 2),
    PLN("PLN", "985", "zł", 2),
    PTE("PTE", "620", "$", 0),
    PYG("PYG", "600", "₲", 0),

    // --- Q ---
    QAR("QAR", "634", "ر.ق", 2),

    // --- R ---
    ROL("ROL", "642", "lei", 0),
    RON("RON", "946", "lei", 2),
    RSD("RSD", "941", "din", 2),
    RUB("RUB", "643", "₽", 2),
    RUR("RUR", "810", "₽", 2),
    RWF("RWF", "646", "₣", 0),

    // --- S ---
    SAR("SAR", "682", "﷼", 2),
    SBD("SBD", "090", "$", 2),
    SCR("SCR", "690", "₨", 2),
    SDD("SDD", "736", "ج.س.", 2),
    SDG("SDG", "938", "ج.س.", 2),
    SEK("SEK", "752", "kr", 2),
    SGD("SGD", "702", "$", 2),
    SHP("SHP", "654", "£", 2),
    SIT("SIT", "705", "SIT", 2),
    SKK("SKK", "703", "Sk", 2),
    SLE("SLE", "925", "Le", 2),
    SLL("SLL", "694", "Le", 2),
    SOS("SOS", "706", "Sh", 2),
    SRD("SRD", "968", "$", 2),
    SRG("SRG", "740", "ƒ", 2),
    SSP("SSP", "728", "£", 2),
    STD("STD", "678", "Db", 2),
    STN("STN", "930", "Db", 2),
    SVC("SVC", "222", "₡", 2),
    SYP("SYP", "760", "£", 2),
    SZL("SZL", "748", "L", 2),

    // --- T ---
    THB("THB", "764", "฿", 2),
    TJS("TJS", "972", "SM", 2),
    TMM("TMM", "795", "T", 2),
    TMT("TMT", "934", "T", 2),
    TND("TND", "788", "د.ت", 3),
    TOP("TOP", "776", "T$", 2),
    TPE("TPE", "626", "$", 0),
    TRL("TRL", "792", "₤", 0),
    TRY("TRY", "949", "₺", 2),
    TTD("TTD", "780", "$", 2),
    TWD("NT$", "901", "$", 2),
    TZS("TZS", "834", "Sh", 2),

    // --- U ---
    UAH("UAH", "980", "₴", 2),
    UGX("UGX", "800", "Sh", 0),
    USD("US$", "840", "$", 2),
    USN("USN", "997", "$", 2),
    USS("USS", "998", "$", 2),
    UYI("UYI", "940", "$", 0),
    UYU("UYU", "858", "$", 2),
    UZS("UZS", "860", "лв", 2),

    // --- V ---
    VEB("VEB", "862", "Bs.", 2),
    VED("VED", "926", "Bs.D", 2),
    VEF("VEF", "937", "Bs.F", 2),
    VES("VES", "928", "Bs.S", 2),
    VND("₫",   "704", "₫", 0),
    VUV("VUV", "548", "Vt", 0),

    // --- W ---
    WST("WST", "882", "T", 2),

    // --- X (Special / Supranational) ---
    XAF("FCFA", "950", "Fr", 0),
    XAG("XAG", "961", "XAG", -1),
    XAU("XAU", "959", "XAU", -1),
    XBA("XBA", "955", "XBA", -1),
    XBB("XBB", "956", "XBB", -1),
    XBC("XBC", "957", "XBC", -1),
    XBD("XBD", "958", "XBD", -1),
    XCD("EC$", "951", "$", 2),
    XDR("XDR", "960", "SDR", -1),
    XFO("XFO", "000", "XFO", -1),
    XFU("XFU", "000", "XFU", -1),
    XOF("F CFA", "952", "Fr", 0),
    XPD("XPD", "964", "XPD", -1),
    XPF("CFPF", "953", "Fr", 0),
    XPT("XPT", "962", "XPT", -1),
    XSU("XSU", "994", "XSU", -1),
    XTS("XTS", "963", "XTS", -1),
    XUA("XUA", "965", "XUA", -1),
    XXX("¤",   "999", "¤", -1),

    // --- Y ---
    YER("YER", "886", "﷼", 2),
    YUM("YUM", "891", "din", 2),

    // --- Z ---
    ZAR("ZAR", "710", "R", 2),
    ZMK("ZMK", "894", "ZK", 2),
    ZMW("ZMW", "967", "ZK", 2),
    ZWD("ZWD", "716", "$", 2),
    ZWL("ZWL", "932", "$", 2),
    ZWN("ZWN", "942", "$", 2),
    ZWR("ZWR", "935", "$", 2);

    final String code;
    final String numericCode;
    final String symbol;
    final int digits;

    Currency(String code, String numericCode, String symbol, int digits) {
        this.code = code;
        this.numericCode = numericCode;
        this.symbol = symbol;
        this.digits = digits;
    }

    public String getLabel() {
        return this.code;
    }

    public String getName() {
        return code;
    }

    public String getNumericCode() {
        return numericCode;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDigits() {
        return digits;
    }
}