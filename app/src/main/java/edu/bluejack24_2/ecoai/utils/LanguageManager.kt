package edu.bluejack24_2.ecoai.utils

import androidx.compose.runtime.mutableStateOf
import java.util.Locale

object LanguageManager {
    fun getSystemDefaultLanguage(): String {
        val lang = Locale.getDefault().language.lowercase(Locale.ROOT)
        return when (lang) {
            "id" -> "ID"
            "en" -> "EN"
            else -> "EN"
        }
    }

    val currentLanguage = mutableStateOf(getSystemDefaultLanguage())
    
    private val translations = mapOf(
        "EN" to mapOf(
            // Landing Screen
            "waste_tracking_made_easy" to "Waste tracking made easy",
            "get_started" to "Get Started",
            "have_account" to "Have an account?",
            "log_in" to "Log In",
            
            // Login Screen
            "welcome_back" to "Welcome Back!",
            "email_address" to "Email address",
            "password_hint" to "Password (8+ characters)",
            "forgotten_password" to "Forgotten your password?",
            "dont_have_account" to "Don't have an account?",
            "register" to "Register",
            "login_success" to "Login success",
            
            // Register Screen
            "first_name" to "First name",
            "last_name" to "Last name",
            "confirm_email" to "Confirm email address",
            "confirm_password" to "Confirm password (8+ characters)",
            "create_account" to "Create account",
            "password_mismatch" to "Password doesn't match",
            "email_mismatch" to "Email doesn't match",
            "registration_successful" to "Registration successful",
            // Added for dialog
            "registration_success" to "Account Created",
            "verification_link_sent" to "Your account has been successfully created and a verification link has been sent to your email.",
            "go_to_login" to "Go to Login",

            // Notification
            "notif_like" to "liked your post",
            "notif_comment" to "commented on your post",
            "notif_follow" to "started following you",

            "time_just_now" to "just now",
            "time_minutes_ago" to "%d minutes ago",
            "time_hours_ago" to "%d hours ago",
            "time_days_ago" to "%d days ago",
            "time_weeks_ago" to "%d weeks ago",
            "time_months_ago" to "%d months ago",
            "time_years_ago" to "%d years ago",
            // Dialogs
            "delete_post_confirmation_message" to "Are you sure you want to delete this post? This action cannot be undone.",
            "no_notifications" to "No notifications yet",
            "notification" to "Notifications",
            
            // Settings Screen
            "settings" to "Settings",
            "push_notifications" to "Push Notifications",
            "push_notifications_desc" to "Stay up to date with all things happening in your app",
            "appearance" to "Appearance",
            "appearance_desc" to "Choose light, dark or system appearance",
            "dark_mode" to "Dark",
            "light_mode" to "Light",
            "language" to "Language",
            "select_language" to "Select language",
            "logout" to "Log Out",
            
            // Profile Screen
            "profile_picture" to "Profile Picture",
            "settings" to "Settings",
            "write_bio_placeholder" to "Write a bio to help people discover you",
            "edit_profile" to "Edit Profile",
            "posts" to "Posts",
            "saved" to "Saved",
            "liked" to "Liked",
            "following" to "Following",
            "followers" to "Followers",
            "likes" to "Likes",
            
            // Edit Profile Screen
            "edit_profile" to "Edit Profile",
            "full_name" to "Full Name",
            "username" to "Username",
            "bio" to "Bio",
            "save" to "Save",
            "saving" to "Saving...",
            "profile_updated" to "Profile updated!",
            "failed_update" to "Failed: ",
            "name_required" to "Name must be at least 4 characters long",
            "bio_required" to "Bio must be at least 3 words long",
            "username_taken" to "Username is already taken",
            "upload_failed" to "Failed to upload image: ",
            
            // Home Screen
            "following" to "Following",
            "for_you" to "For You",
            "search" to "Search",
            "no_posts_following" to "No posts from users you follow.",
            "unknown_user" to "Unknown",
            "top_comments" to "Top Comments",
            
            // Search Screen
            "search_placeholder" to "Search tags or posts",
            "search_button" to "Search",
            "post_thumbnail" to "Post Thumbnail",
            
            // Post Detail Screen
            "back" to "Back",
            "more" to "More",
            "post_image" to "Post Image",
            "follow" to "Follow",
            "following" to "Following",
            "unfollow" to "Unfollow",
            "comments" to "comments",
            "no_comments" to "No comments yet",
            "reply" to "Reply",
            "like_comment" to "Like comment",
            "add_comment_placeholder" to "Add comment...",
            "like" to "Like",
            "save" to "Save",
            "unsave" to "Unsave",
            "download_photo" to "Download photo",
            "edit" to "Edit",
            "delete" to "Delete",
            "delete_confirmation" to "Delete?",
            "cancel" to "Cancel",
            
            // Progress Screen
            "progress_title" to "Progress",
            "recently_uploaded" to "Recently uploaded",
            "no_items_uploaded" to "No items uploaded yet.",
            "add_waste" to "Add Waste",
            "carbon_footprint" to "Carbon Footprint",
            "carbon_left" to "Carbon left",
            "items_uploaded" to "items uploaded",
            "scan_waste" to "Scan Waste",
            "waste_database" to "Waste Database",
            
            // Create Post Screen
            "back" to "Back",
            "create_new_post" to "Create New Post",
            "no_media_yet" to "No media yet",
            "remove_image" to "Remove image",
            "headline_optional" to "Headline",
            "caption_and_tags_optional" to "Caption and tags",
            "uploading" to "Uploading...",
            "add_image" to "Add Image",
            "post_button" to "Post",
            "post_created" to "Post created!",
            "max_images_reached" to "Maximum 10 images allowed",
            "no_image_selected" to "No image selected",
            
            // Add Waste Screen
            "add_waste_title" to "Add Waste",
            "waste_name" to "Waste Name",
            "enter_waste_name" to "Please enter waste name",
            "camera_permission_error_title" to "Camera Access Denied",
            "camera_permission_error_message" to "You need to allow camera access to use this feature.",
            "open_camera" to "Open Camera",
            "open_gallery" to "Open Gallery",
            "open_history" to "Open History",
            "error_occurred" to "An error occurred",

            // Waste Detail
            "disposal_method" to "Disposal Method",

            // History Screen
            "history" to "History",
            "search_history" to "Search history...",
            "no_history_yet" to "No history yet",
            "no_results_found" to "No results found",
            "no_items_available" to "No items available",

            // Bottom Navigation Bar
            "home" to "Home",
            "progress" to "Progress",
            "create" to "Create",
            "notification" to "Notification",
            "profile" to "Profile",
            
            // Forgot Password Screen
            "forgot_password_title" to "Forgot Password",
            "enter_email_address" to "Enter Email Address",
            "send_reset_link" to "Send Reset Link",
            
            // Create New Password Screen
            "create_new_password_title" to "Create New Password",
            "new_password" to "New Password",
            "confirm_password" to "Confirm Password",
            "confirm_button" to "Confirm",
            "success" to "Success",
            "reset_password_success_message" to "If this email is associated with an account, you will receive a password reset link shortly.",
            "ok" to "OK"
        ),
        "ID" to mapOf(
            // Landing Screen
            "waste_tracking_made_easy" to "Pelacakan sampah jadi mudah",
            "get_started" to "Mulai",
            "have_account" to "Sudah punya akun?",
            "log_in" to "Masuk",
            
            // Login Screen
            "welcome_back" to "Selamat Datang Kembali!",
            "email_address" to "Alamat email",
            "password_hint" to "Kata sandi (8+ karakter)",
            "forgotten_password" to "Lupa kata sandi?",
            "dont_have_account" to "Belum punya akun?",
            "register" to "Daftar",
            "login_success" to "Login berhasil",
            
            // Register Screen
            "first_name" to "Nama depan",
            "last_name" to "Nama belakang",
            "confirm_email" to "Konfirmasi alamat email",
            "confirm_password" to "Konfirmasi kata sandi (8+ karakter)",
            "create_account" to "Buat akun",
            "password_mismatch" to "Kata sandi tidak cocok",
            "email_mismatch" to "Email tidak cocok",
            "registration_successful" to "Registrasi berhasil",
            // Added for dialog
            "registration_success" to "Akun Berhasil Dibuat",
            "verification_link_sent" to "Akun Anda berhasil dibuat dan tautan verifikasi telah dikirim ke email Anda.",
            "go_to_login" to "Ke Halaman Masuk",

            // Notification
            "notif_like" to "menyukai postingan kamu",
            "notif_comment" to "mengomentari postingan kamu",
            "notif_follow" to "mulai mengikuti kamu",

            "time_just_now" to "baru saja",
            "time_minutes_ago" to "%d menit yang lalu",
            "time_hours_ago" to "%d jam yang lalu",
            "time_days_ago" to "%d hari yang lalu",
            "time_weeks_ago" to "%d minggu yang lalu",
            "time_months_ago" to "%d bulan yang lalu",
            "time_years_ago" to "%d tahun yang lalu",
            // Dialogs
            "delete_post_confirmation_message" to "Apakah Anda yakin ingin menghapus postingan ini? Tindakan ini tidak dapat dibatalkan.",

            "no_notifications" to "Belum ada notifikasi",
            
            // Settings Screen
            "settings" to "Pengaturan",
            "push_notifications" to "Notifikasi",
            "push_notifications_desc" to "Dapatkan pembaruan tentang semua yang terjadi di aplikasi Anda",
            "appearance" to "Tampilan",
            "appearance_desc" to "Pilih tampilan terang, gelap, atau ikuti sistem",
            "dark_mode" to "Gelap",
            "light_mode" to "Terang",
            "language" to "Bahasa",
            "select_language" to "Pilih bahasa",
            "logout" to "Keluar",
            "notification" to "Notifikasi",
            
            // Profile Screen
            "profile_picture" to "Foto Profil",
            "settings" to "Pengaturan",
            "write_bio_placeholder" to "Tulis bio untuk membantu orang menemukan Anda",
            "edit_profile" to "Sunting Profil",
            "posts" to "Postingan",
            "saved" to "Disimpan",
            "liked" to "Disukai",
            "following" to "Mengikuti",
            "followers" to "Pengikut",
            "likes" to "Suka",
            
            // Edit Profile Screen
            "edit_profile" to "Sunting Profil",
            "full_name" to "Nama Lengkap",
            "username" to "Nama Pengguna",
            "bio" to "Bio",
            "save" to "Simpan",
            "saving" to "Menyimpan...",
            "profile_updated" to "Profil berhasil diperbarui!",
            "failed_update" to "Gagal: ",
            "name_required" to "Nama harus minimal 4 karakter",
            "bio_required" to "Bio harus minimal 3 kata",
            "username_taken" to "Nama pengguna sudah digunakan",
            "upload_failed" to "Gagal mengunggah gambar: ",
            
            // Home Screen
            "following" to "Mengikuti",
            "for_you" to "Untukmu",
            "search" to "Cari",
            "no_posts_following" to "Tidak ada postingan dari pengguna yang Anda ikuti.",
            "unknown_user" to "Tidak Dikenal",
            "top_comments" to "Komentar Teratas",

            // Search Screen
            "search_placeholder" to "Cari tag atau postingan",
            "search_button" to "Cari",
            "post_thumbnail" to "Thumbnail Postingan",
            
            // Post Detail Screen
            "back" to "Kembali",
            "more" to "Lainnya",
            "post_image" to "Gambar Postingan",
            "follow" to "Ikuti",
            "following" to "Mengikuti",
            "unfollow" to "Berhenti Mengikuti",
            "comments" to "komentar",
            "no_comments" to "Belum ada komentar",
            "reply" to "Balas",
            "like_comment" to "Sukai komentar",
            "add_comment_placeholder" to "Tambah komentar...",
            "like" to "Suka",
            "save" to "Simpan",
            "unsave" to "Batal Simpan",
            "download_photo" to "Unduh foto",
            "edit" to "Ubah",
            "delete" to "Hapus",
            "delete_confirmation" to "Hapus?",
            "cancel" to "Batal",
            
            // Progress Screen
            "progress_title" to "Progres",
            "recently_uploaded" to "Baru diunggah",
            "no_items_uploaded" to "Belum ada item yang diunggah.",
            "add_waste" to "Tambah Sampah",
            "carbon_footprint" to "Jejak Karbon",
            "carbon_left" to "Sisa karbon",
            "items_uploaded" to "item yang diunggah",
            "scan_waste" to "Pindai Sampah",
            "waste_database" to "Database Sampah",

            // Create Post Screen
            "back" to "Kembali",
            "create_new_post" to "Buat Postingan Baru",
            "no_media_yet" to "Belum ada media",
            "remove_image" to "Hapus gambar",
            "headline_optional" to "Judul",
            "caption_and_tags_optional" to "Keterangan dan tag",
            "uploading" to "Mengunggah...",
            "add_image" to "Tambah Gambar",
            "post_button" to "Unggah",
            "post_created" to "Postingan berhasil dibuat!",
            "max_images_reached" to "Maksimal 10 gambar yang diizinkan",
            "no_image_selected" to "Tidak ada gambar yang dipilih",
            
            // Add Waste Screen
            "add_waste_title" to "Tambah Sampah",
            "waste_name" to "Nama Sampah",
            "enter_waste_name" to "Masukkan nama sampah",
            "camera_permission_error_title" to "Akses Kamera Ditolak",
            "camera_permission_error_message" to "Anda perlu mengizinkan akses kamera untuk menggunakan fitur ini.",
            "open_camera" to "Buka Kamera",
            "open_gallery" to "Buka Galeri",
            "open_history" to "Buka Riwayat",
            "error_occurred" to "Terjadi kesalahan",

            // Waste Detail
            "disposal_method" to "Metode Pembuangan",

            // History Screen
            "history" to "Riwayat",
            "search_history" to "Cari riwayat...",
            "no_history_yet" to "No history yet",
            "no_results_found" to "No results found",
            "no_items_available" to "Tidak ada item tersedia",

            // Bottom Navigation Bar
            "home" to "Beranda",
            "progress" to "Progres",
            "create" to "Buat",
            "notification" to "Notifikasi",
            "profile" to "Profil",
            
            // Forgot Password Screen
            "forgot_password_title" to "Lupa Kata Sandi",
            "enter_email_address" to "Masukkan Alamat Email",
            "send_reset_link" to "Kirim Tautan Reset",
            
            // Create New Password Screen
            "create_new_password_title" to "Buat Kata Sandi Baru",
            "new_password" to "Kata Sandi Baru",
            "confirm_password" to "Konfirmasi Kata Sandi",
            "confirm_button" to "Konfirmasi",
            "success" to "Berhasil",
            "reset_password_success_message" to "Jika email ini terdaftar, Anda akan menerima tautan untuk mengatur ulang kata sandi segera.",
            "ok" to "Oke"

        )
    )
    
    fun getString(key: String): String {
        return translations[currentLanguage.value]?.get(key) ?: key
    }
    
    fun setLanguage(language: String) {
        currentLanguage.value = language
    }
}
