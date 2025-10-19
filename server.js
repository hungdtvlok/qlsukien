// server.js

const express = require("express");
const nodemailer = require("nodemailer");

const app = express();
app.use(express.json());

// ======= CẤU HÌNH GMAIL =======
const transporter = nodemailer.createTransport({
    host: "smtp.gmail.com",
    port: 465,
    secure: true,
    auth: {
        user: "githich462@gmail.com",      // Gmail của bạn
        pass: "aqzzbtyfarsgaesd",         // App Password Gmail
    },
});

// Kiểm tra kết nối SMTP
transporter.verify((err, success) => {
    if (err) console.error("❌ Lỗi cấu hình Gmail:", err);
    else console.log("✅ Gmail SMTP sẵn sàng để gửi email!");
});

// ======= API gửi mail =======
app.post("/api/testmail", async (req, res) => {
    const { to, subject, text } = req.body;

    if (!to || !subject || !text) {
        return res.status(400).json({ message: "Thiếu thông tin gửi mail" });
    }

    try {
        const info = await transporter.sendMail({
            from: `"Test Node.js" <githich462@gmail.com>`,
            to,
            subject,
            text,
            html: `<h2>${subject}</h2><p>${text}</p>`
        });

        console.log("✅ Mail gửi thành công:", info.messageId);
        res.json({ message: "Mail đã gửi thành công!", info });
    } catch (err) {
        console.error("❌ Lỗi gửi mail:", err);
        res.status(500).json({ message: "Gửi mail thất bại", error: err.toString() });
    }
});

// ======= Route test server =======
app.get("/", (req, res) => {
    res.send("✅ Server đang chạy OK!");
});

// ======= START SERVER =======
const PORT = 5000;
app.listen(PORT, () => {
    console.log(`✅ Server chạy tại http://localhost:${PORT}`);
});
