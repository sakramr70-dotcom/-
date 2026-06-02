package com.example.data

data class SurahInfo(
    val number: Int,
    val nameAr: String,
    val nameEn: String,
    val ayahs: Int
)

data class DefaultTaskTemplate(
    val order: Int,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val emoji: String,
    val starsReward: Int,
    val requiresProof: Boolean,
    val frequency: String,
    val isDefault: Boolean
)

data class DefaultRewardTemplate(
    val nameAr: String,
    val nameEn: String,
    val emoji: String,
    val starsCost: Int,
    val category: String,
    val isAvailable: Boolean
)

data class AvatarOption(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val emoji: String
)

data class AchievementBadge(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val emoji: String,
    val color: String,
    val triggerType: String, // "streak", "prayer-streak", "task-count", "total-stars", "recitation-count"
    val triggerValue: Int,
    val relatedId: String? = null
)

object NujoomConstants {
    val DEFAULT_TASKS = listOf(
        DefaultTaskTemplate(
            order = 1,
            nameAr = "تنظيف الأسنان",
            nameEn = "Brush Teeth",
            descriptionAr = "نظّف أسنانك لمدة دقيقتين",
            descriptionEn = "Brush your teeth for 2 minutes",
            emoji = "🪥",
            starsReward = 1,
            requiresProof = true,
            frequency = "daily",
            isDefault = true
        ),
        DefaultTaskTemplate(
            order = 2,
            nameAr = "ترتيب غرفتي",
            nameEn = "Tidy My Room",
            descriptionAr = "رتّب سريرك وغرفتك بشكل كامل",
            descriptionEn = "Make your bed and tidy your room completely",
            emoji = "🛏️",
            starsReward = 5,
            requiresProof = true,
            frequency = "daily",
            isDefault = true
        ),
        DefaultTaskTemplate(
            order = 3,
            nameAr = "ممارسة الرياضة",
            nameEn = "Exercise",
            descriptionAr = "مارس أي نشاط رياضي لمدة ٢٠ دقيقة",
            descriptionEn = "Do any physical activity for 20 minutes",
            emoji = "🏃",
            starsReward = 7,
            requiresProof = false,
            frequency = "daily",
            isDefault = true
        ),
        DefaultTaskTemplate(
            order = 4,
            nameAr = "المساعدة في المنزل",
            nameEn = "Help at Home",
            descriptionAr = "ساعد في المنزل مثل تنظيف الطاولة أو جلب الطلبات",
            descriptionEn = "Help around the house, like clearing the table or running errands",
            emoji = "🧹",
            starsReward = 10,
            requiresProof = false,
            frequency = "daily",
            isDefault = true
        ),
        DefaultTaskTemplate(
            order = 5,
            nameAr = "قراءة قصة",
            nameEn = "Read a Story",
            descriptionAr = "اقرأ قصة كاملة أو فصلاً من كتاب",
            descriptionEn = "Read a complete story or a chapter from a book",
            emoji = "📚",
            starsReward = 3,
            requiresProof = false,
            frequency = "daily",
            isDefault = true
        )
    )

    val DEFAULT_REWARDS = listOf(
        DefaultRewardTemplate("آيس كريم", "Ice Cream", "🍦", 10, "food", true),
        DefaultRewardTemplate("وقت شاشة إضافي (٣٠ دقيقة)", "Extra Screen Time (30 min)", "📱", 20, "screen", true),
        DefaultRewardTemplate("ذهاب للملاهي 🎉", "Amusement Park Trip 🎉", "🎡", 30, "outing", true),
        DefaultRewardTemplate("تذكرة سينما", "Cinema Ticket", "🎬", 50, "outing", true),
        DefaultRewardTemplate("لعبة جديدة", "New Game", "🎮", 100, "toy", true),
        DefaultRewardTemplate("رحلة عائلية", "Family Trip", "🏖️", 200, "experience", true),
        DefaultRewardTemplate("مجموعة ألوان مائية 🎨", "Watercolor Painting Set 🎨", "🎨", 25, "learning", true),
        DefaultRewardTemplate("يوم بدون مهام 🎉", "Task-Free Day", "🎉", 40, "freedom", true),
        DefaultRewardTemplate("وجبة سريعة مفضّلة", "Favorite Fast Food Meal", "🍔", 35, "food", true),
        DefaultRewardTemplate("ساعة ألعاب إضافية", "Extra Play Hour", "⚽", 15, "activity", true)
    )

    val AVATARS = listOf(
        AvatarOption("boy_glasses", "ولد ذكي 🤓", "Smart Boy 🤓", "🤓"),
        AvatarOption("girl_hijab", "بنت خجولة 🧕", "Modest Girl 🧕", "🧕"),
        AvatarOption("boy_sporty", "ولد رياضي ⚽", "Sporty Boy ⚽", "⚽"),
        AvatarOption("girl_bookworm", "بنت قارئة 📚", "Reading Girl 📚", "📚"),
        AvatarOption("kid_artist", "فنان صغير 🎨", "Little Artist 🎨", "🎨"),
        AvatarOption("boy_gamer", "ولد قيمر 🎮", "Gamer Boy 🎮", "🎮"),
        AvatarOption("girl_coder", "عبقرية برمجية 💻", "Coder Girl 💻", "💻"),
        AvatarOption("kid_chef", "طباخ ماهر 🍳", "Master Chef 🍳", "🍳"),
        AvatarOption("boy_cool", "ولد رائع 😎", "Cool Boy 😎", "😎"),
        AvatarOption("girl_happy", "بنت سعيدة 👧", "Happy Girl 👧", "👧"),
        AvatarOption("boy_adventurer", "مكتشف مغامر 🤠", "Adventurer 🤠", "🤠"),
        AvatarOption("girl_dreamer", "حالمة صغيرة 🦄", "Little Dreamer 🦄", "🦄"),
        AvatarOption("kid_scientist", "عالم المستقبل 🔬", "Future Scientist 🔬", "🔬"),
        AvatarOption("boy_astronaut", "رائد فضاء 🚀", "Astronaut Boy 🚀", "🚀"),
        AvatarOption("girl_nature", "صديقة الطبيعة 🌱", "Nature Lover 🌱", "🌱"),
        AvatarOption("kid_music", "عازف متميز 🎵", "Musician Kid 🎵", "🎵")
    )

    val FRAMES = listOf(
        "#FFB800" to "Glow Gold",
        "#7C3AED" to "Electric Purple",
        "#3B82F6" to "Ocean Blue",
        "#FF6B9D" to "Sweet Pink",
        "#22C55E" to "Emerald Green",
        "#F97316" to "Sunset Orange"
    )

    val ACHIEVEMENT_BADGES = listOf(
        AchievementBadge(
            id = "beginner-star",
            nameAr = "نجم مبتدئ 🏅",
            nameEn = "Beginner Star",
            descriptionAr = "البداية نحو العظمة! جمعت أول 10 نجوم بجدارة",
            descriptionEn = "First step to greatness! Earned your first 10 stars",
            emoji = "🌱",
            color = "#3B82F6",
            triggerType = "total-stars",
            triggerValue = 10
        ),
        AchievementBadge(
            id = "shining-star",
            nameAr = "نجم لامع 🌟",
            nameEn = "Shining Star",
            descriptionAr = "لقد أصبحت نجماً لامعاً بجمع 100 نجمة كاملة!",
            descriptionEn = "You became a shining star with 100 total stars!",
            emoji = "✨",
            color = "#F97316",
            triggerType = "total-stars",
            triggerValue = 100
        ),
        AchievementBadge(
            id = "week-star",
            nameAr = "نجم الأسبوع ⭐",
            nameEn = "Star of the Week",
            descriptionAr = "أنجزت جميع مهامك لـ 7 أيام متواصلة",
            descriptionEn = "Completed all your tasks for 7 consecutive days",
            emoji = "🌟",
            color = "#FFB800",
            triggerType = "streak",
            triggerValue = 7
        ),
        AchievementBadge(
            id = "prayer-consistent",
            nameAr = "المصلي المواظب 🕌",
            nameEn = "Consistent Prayer",
            descriptionAr = "صلّيت الصلوات الخمس كاملة لـ 5 أيام متواصلة",
            descriptionEn = "Prayed all 5 daily prayers for 5 consecutive days",
            emoji = "🕌",
            color = "#059669",
            triggerType = "prayer-streak",
            triggerValue = 5
        ),
        AchievementBadge(
            id = "reader",
            nameAr = "القارئ النشيط 📚",
            nameEn = "Active Reader",
            descriptionAr = "أنجزت مهمة القراءة 5 مرات",
            descriptionEn = "Completed reading tasks 5 times",
            emoji = "📚",
            color = "#7C3AED",
            triggerType = "task-count",
            triggerValue = 5,
            relatedId = "read-story"
        ),
        AchievementBadge(
            id = "tidy-room",
            nameAr = "المرتّب الصغير 🛏️",
            nameEn = "Tidy Kid",
            descriptionAr = "رتّبت غرفتك 5 مرات",
            descriptionEn = "Tidied your room 5 times",
            emoji = "🛏️",
            color = "#2563EB",
            triggerType = "task-count",
            triggerValue = 5,
            relatedId = "tidy-room"
        ),
        AchievementBadge(
            id = "star-collector-50",
            nameAr = "جامع النجوم 🌟",
            nameEn = "Star Collector",
            descriptionAr = "جمعت 50 نجمة إجمالاً",
            descriptionEn = "Earned 50 stars in total",
            emoji = "💫",
            color = "#F59E0B",
            triggerType = "total-stars",
            triggerValue = 50
        ),
        AchievementBadge(
            id = "quran-reciter",
            nameAr = "تالي القرآن 📖",
            nameEn = "Quran Reciter",
            descriptionAr = "سجّلت 3 تلاوات قرآنية",
            descriptionEn = "Recorded 3 Quran recitations",
            emoji = "📿",
            color = "#065F46",
            triggerType = "recitation-count",
            triggerValue = 3
        )
    )

    val QURAN_SURAHS = listOf(
        SurahInfo(1, "الفاتحة", "Al-Fatihah", 7),
        SurahInfo(2, "البقرة", "Al-Baqarah", 286),
        SurahInfo(3, "آل عمران", "Al-Imran", 200),
        SurahInfo(4, "النساء", "An-Nisa'", 176),
        SurahInfo(5, "المائدة", "Al-Ma'idah", 120),
        SurahInfo(6, "الأنعام", "Al-An'am", 165),
        SurahInfo(7, "الأعراف", "Al-A'raf", 206),
        SurahInfo(8, "الأنفال", "Al-Anfal", 75),
        SurahInfo(9, "التوبة", "At-Tawbah", 129),
        SurahInfo(10, "يونس", "Yunus", 109),
        SurahInfo(11, "هود", "Hud", 123),
        SurahInfo(12, "يوسف", "Yusuf", 111),
        SurahInfo(13, "الرعد", "Ar-Ra'd", 43),
        SurahInfo(14, "إبراهيم", "Ibrahim", 52),
        SurahInfo(15, "الحجر", "Al-Hijr", 99),
        SurahInfo(16, "النحل", "An-Nahl", 128),
        SurahInfo(17, "الإسراء", "Al-Isra'", 111),
        SurahInfo(18, "الكهف", "Al-Kahf", 110),
        SurahInfo(19, "مريم", "Maryam", 98),
        SurahInfo(20, "طه", "Taha", 135),
        SurahInfo(21, "الأنبياء", "Al-Anbiya'", 112),
        SurahInfo(22, "الحج", "Al-Hajj", 78),
        SurahInfo(23, "المؤمنون", "Al-Mu'minun", 118),
        SurahInfo(24, "النور", "An-Nur", 64),
        SurahInfo(25, "الفرقان", "Al-Furqan", 77),
        SurahInfo(26, "الشعراء", "Ash-Shu'ara'", 227),
        SurahInfo(27, "النمل", "An-Naml", 93),
        SurahInfo(28, "القصص", "Al-Qasas", 88),
        SurahInfo(29, "العنكبوت", "Al-Ankabut", 69),
        SurahInfo(30, "الروم", "Ar-Rum", 60),
        SurahInfo(31, "لقمان", "Luqman", 34),
        SurahInfo(32, "السجدة", "As-Sajdah", 30),
        SurahInfo(33, "الأحزاب", "Al-Ahzab", 73),
        SurahInfo(34, "سبأ", "Saba'", 54),
        SurahInfo(35, "فاطر", "Fatir", 45),
        SurahInfo(36, "يس", "Ya-Sin", 83),
        SurahInfo(37, "الصافات", "As-Saffat", 182),
        SurahInfo(38, "ص", "Sad", 88),
        SurahInfo(39, "الزمر", "Az-Zumar", 75),
        SurahInfo(40, "غافر", "Ghafir", 85),
        SurahInfo(41, "فصلت", "Fussilat", 54),
        SurahInfo(42, "الشورى", "Ash-Shura", 53),
        SurahInfo(43, "الزخرف", "Az-Zukhruf", 89),
        SurahInfo(44, "الدخان", "Ad-Dukhan", 59),
        SurahInfo(45, "الجاثية", "Al-Jathiyah", 37),
        SurahInfo(46, "الأحقاف", "Al-Ahqaf", 35),
        SurahInfo(47, "محمد", "Muhammad", 38),
        SurahInfo(48, "الفتح", "Al-Fath", 29),
        SurahInfo(49, "الحجرات", "Al-Hujurat", 18),
        SurahInfo(50, "ق", "Qaf", 45),
        SurahInfo(51, "الذاريات", "Adh-Dhariyat", 60),
        SurahInfo(52, "الطور", "At-Tur", 49),
        SurahInfo(53, "النجم", "An-Najm", 62),
        SurahInfo(54, "القمر", "Al-Qamar", 55),
        SurahInfo(55, "الرحمن", "Ar-Rahman", 78),
        SurahInfo(56, "الواقعة", "Al-Waqi'ah", 96),
        SurahInfo(57, "الحديد", "Al-Hadid", 29),
        SurahInfo(58, "المجادلة", "Al-Mujadilah", 22),
        SurahInfo(59, "الحشر", "Al-Hashr", 24),
        SurahInfo(60, "الممتحنة", "Al-Mumtahanah", 13),
        SurahInfo(61, "الصف", "As-Saff", 14),
        SurahInfo(62, "الجمعة", "Al-Jumua", 11),
        SurahInfo(63, "المنافقون", "Al-Munafiqun", 11),
        SurahInfo(64, "التغابن", "At-Taghabun", 18),
        SurahInfo(65, "الطلاق", "At-Talaq", 12),
        SurahInfo(66, "التحريم", "At-Tahrim", 12),
        SurahInfo(67, "الملك", "Al-Mulk", 30),
        SurahInfo(68, "القلم", "Al-Qalam", 52),
        SurahInfo(69, "الحاقة", "Al-Haqqah", 52),
        SurahInfo(70, "المعارج", "Al-Ma'arij", 44),
        SurahInfo(71, "نوح", "Nuh", 28),
        SurahInfo(72, "الجن", "Al-Jinn", 28),
        SurahInfo(73, "المزمل", "Al-Muzzammil", 20),
        SurahInfo(74, "المدثر", "Al-Muddaththir", 56),
        SurahInfo(75, "القيامة", "Al-Qiyamah", 40),
        SurahInfo(76, "الإنسان", "Al-Insan", 31),
        SurahInfo(77, "المرسلات", "Al-Mursalat", 50),
        SurahInfo(78, "النبأ", "An-Naba'", 40),
        SurahInfo(79, "النازعات", "An-Nazi'at", 46),
        SurahInfo(80, "عبس", "Abasa", 42),
        SurahInfo(81, "التكوير", "At-Takwir", 29),
        SurahInfo(82, "الانفطار", "Al-Infitar", 19),
        SurahInfo(83, "المطففين", "Al-Mutaffifin", 36),
        SurahInfo(84, "الانشقاق", "Al-Inshiqaq", 25),
        SurahInfo(85, "البروج", "Al-Buruj", 22),
        SurahInfo(86, "الطارق", "At-Tariq", 17),
        SurahInfo(87, "الأعلى", "Al-A'la", 19),
        SurahInfo(88, "الغاشية", "Al-Ghashiyah", 26),
        SurahInfo(89, "الفجر", "Al-Fajr", 30),
        SurahInfo(90, "البلد", "Al-Balad", 20),
        SurahInfo(91, "الشمس", "Ash-Shams", 15),
        SurahInfo(92, "الليل", "Al-Layl", 21),
        SurahInfo(93, "الضحى", "Ad-Duha", 11),
        SurahInfo(94, "الشرح", "Ash-Sharh", 8),
        SurahInfo(95, "التين", "At-Tin", 8),
        SurahInfo(96, "العلق", "Al-Alaq", 19),
        SurahInfo(97, "القدر", "Al-Qadr", 5),
        SurahInfo(98, "البينة", "Al-Bayyinah", 8),
        SurahInfo(99, "الزلزلة", "Az-Zalzalah", 8),
        SurahInfo(100, "العاديات", "Al-Adiyat", 11),
        SurahInfo(101, "القارعة", "Al-Qari'ah", 11),
        SurahInfo(102, "التكاثر", "At-Takathur", 8),
        SurahInfo(103, "العصر", "Al-Asr", 3),
        SurahInfo(104, "الهمزة", "Al-Humazah", 9),
        SurahInfo(105, "الفيل", "Al-Fil", 5),
        SurahInfo(106, "قريش", "Quraysh", 4),
        SurahInfo(107, "الماعون", "Al-Ma'un", 7),
        SurahInfo(108, "الكوثر", "Al-Kauthar", 3),
        SurahInfo(109, "الكافرون", "Al-Kafirun", 6),
        SurahInfo(110, "النصر", "An-Nasr", 3),
        SurahInfo(111, "المسد", "Al-Masad", 5),
        SurahInfo(112, "الإخلاص", "Al-Ikhlas", 4),
        SurahInfo(113, "الفلق", "Al-Falaq", 5),
        SurahInfo(114, "الناس", "An-Nas", 6)
    )
}
