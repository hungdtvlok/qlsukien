const express = require("express");
const nodemailer = require("nodemailer");

const app = express();
app.use(express.json());

const transporter = nodemailer.createTransport({
    host: "smtp.gmail.com",
    port: 465,
    secure: true,
    auth: {
        user: "githich462@gmail.com",
        pass: "aqzzbtyfarsgaesd",
    },
});

app.post("/api/quenmk", async (req, res) => {
    const { to, subject, text } = req.body;

    try {
        const info = await transporter.sendMail({
            from: `"Ban Tổ Chức" <githich462@gmail.com>`,
            to,
            subject,
            text,
        });
        res.json({ message: "Gửi mail thành công!", info });
    } catch (err) {
        res.status(500).json({ message: "Gửi mail thất bại", error: err.toString() });
    }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server chạy tại port ${PORT}`));
