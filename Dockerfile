# جلب صورة جافا جاهزة
FROM openjdk:17-jdk-alpine
# تحديد مكان الشغل جوه السيرفر
WORKDIR /app
# نسخ ملف الـ jar اللي السيرفر هيعمله
COPY target/*.jar app.jar
# أمر تشغيل المشروع
ENTRYPOINT ["java","-jar","app.jar"]