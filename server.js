// server.js
const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");

const app = express();

// Middleware
app.use(express.json()); // thay bodyParser
app.use(cors());

// 🔑 SECRET KEY cho JWT
const JWT_SECRET = "secret123";

// Kết nối MongoDB
mongoose.connect("mongodb+srv://bdx:123456789%40@cluster0.xmmbfgf.mongodb.net/qlsukien?retryWrites=true&w=majority", {
    useNewUrlParser: true,
    useUnifiedTopology: true
}).then(() => console.log("MongoDB connected"))
  .catch(err => console.log("MongoDB connection error:", err));

// Schema và Model cho người dùng
const userSchema = new mongoose.Schema({
    username: { type: String, required: true, unique: true },
    email: { type: String, required: true, unique: true },
    phone: { type: String, required: true },
    password: { type: String, required: true }
});

const User = mongoose.model("User", userSchema);

// API đăng ký
app.post("/register", async (req, res) => {
    const { username, email, phone, password, confirmPassword } = req.body;

    // Kiểm tra đầy đủ thông tin
    if (!username || !email || !phone || !password || !confirmPassword) {
        return res.status(400).json({ message: "Vui lòng điền đầy đủ thông tin." });
    }

    // Kiểm tra mật khẩu xác nhận
    if (password !== confirmPassword) {
        return res.status(400).json({ message: "Mật khẩu không khớp." });
    }

    // Validate email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        return res.status(400).json({ message: "Email không hợp lệ." });
    }

    // Validate phone
    const phoneRegex = /^[0-9]{9,12}$/;
    if (!phoneRegex.test(phone)) {
        return res.status(400).json({ message: "Số điện thoại không hợp lệ." });
    }

    try {
        // Kiểm tra username hoặc email đã tồn tại chưa
        const existingUser = await User.findOne({ $or: [{ username }, { email }] });
        if (existingUser) {
            return res.status(400).json({ message: "Tên đăng nhập hoặc email đã tồn tại." });
        }

        // Hash mật khẩu
        const hashedPassword = await bcrypt.hash(password, 10);

        // Tạo user mới
        const newUser = new User({
            username,
            email,
            phone,
            password: hashedPassword
        });

        await newUser.save();

        // Tạo JWT token (nếu muốn)
        const token = jwt.sign({ id: newUser._id }, JWT_SECRET, { expiresIn: "7d" });

        return res.status(201).json({ message: "Đăng ký thành công!", token });

    } catch (err) {
        console.error(err);
        return res.status(500).json({ message: "Lỗi server." });
    }
});

// Server chạy trên port 5000
app.listen(5000, () => {
    console.log("Server running on http://localhost:5000");
});
