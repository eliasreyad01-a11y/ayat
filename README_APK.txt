AYAT — مشروع Android TV (WebView)

لم يُبنَ هذا المشروع ولم يُجرَّب على جهاز: كُتب بدون Android SDK. أول بناء عندك.

البناء:
1) ثبّت Android Studio (الإصدار الحديث) وافتح هذا المجلد (AYAT-Android).
2) انتظر انتهاء Gradle Sync (يحتاج إنترنت لتنزيل المكتبات).
3) Build > Build Bundle(s) / APK(s) > Build APK(s)
4) الملف الناتج: app/build/outputs/apk/debug/app-debug.apk

التثبيت على التلفزيون:
- فعّل خيارات المطور (اضغط على Build number سبع مرات) ثم ADB debugging / Network debugging.
- من الكمبيوتر:  adb connect IP_التلفزيون:5555   ثم   adb install -r app-debug.apk
- أو انسخ الملف بفلاشة أو بتطبيق "Send Files to TV" وافتحه (يلزم السماح بمصادر غير معروفة).

ملاحظات:
- الصفحة داخل app/src/main/assets/index.html. لتحديث التطبيق استبدل هذا الملف وأعد البناء.
- صور القرّاء (اختياري): app/src/main/assets/img/reciters/ (afs.jpg, sds.jpg, shur.jpg, ...).
- يحتاج التطبيق إنترنت (صوت everyayah.com ونص api.alquran.cloud وخطوط Google).
- إذا بدا التصميم غير سليم على تلفزيون قديم، حدّث Android System WebView.
- نسخة debug للتجربة الشخصية. للنشر في المتجر تحتاج توقيعًا (release) واحترام تراخيص الصوت والنص.
