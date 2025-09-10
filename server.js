const express = require("express");
const mongoose = require("mongoose");
const bodyParser = require("body-parser");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");

const app = express();
app.use(bodyParser.json());
app.use(cors());

// 🔑 SECRET KEY cho JWT
const JWT_SECRET = "secret123";

// Kết nối MongoDB
mongoose.connect("ngodb+srv://bdx:123456789%40@cluster0.xmmbfgf.mongodb.net/qlsukien?retryWrites=true&w=majority&appName=Cluster0"; ", {
    useNewUrlParser: true,
    useUnifiedTopology: true
}).then(() => console.log("✅ Kết nối MongoDB thành công"))
    .catch(err => console.log("❌ Lỗi kết nối MongoDB:", err));

// Schema người dùng
const UserSchema = new mongoose.Schema({
    username: { type: String, required: true, unique: true },
    email: String,
    phone: String,
    password: { type: String, required: true }
});

const User = mongoose.model("User", UserSchema);

//// ======================= API =======================

// 🟢 API Đăng ký
app.post("/api/register", async (req, res) => {
    try {
        const { username, email, phone, password } = req.body;

        // Kiểm tra username trùng
        const existUser = await User.findOne({ username });
        if (existUser) {
            return res.status(400).json({ message: "Tên đăng nhập đã tồn tại" });
        }

        // Mã hoá password
        const hashedPassword = await bcrypt.hash(password, 10);

        const newUser = new User({
            username,
            email,
            phone,
            password: hashedPassword
        });

        await newUser.save();
        res.json({ message: "Đăng ký thành công" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 🟢 API Đăng nhập
app.post("/api/login", async (req, res) => {
    try {
        const { username, password } = req.body;

        // Tìm user
        const user = await User.findOne({ username });
        if (!user) {
            return res.status(400).json({ message: "Sai tên đăng nhập" });
        }

        // So sánh password
        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(400).json({ message: "Sai mật khẩu" });
        }

        // Tạo token JWT
        const token = jwt.sign({ id: user._id }, JWT_SECRET, { expiresIn: "1h" });

        res.json({
            message: "Đăng nhập thành công",
            token,
            user: { username: user.username, email: user.email, phone: user.phone }
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// 🟢 API Lấy danh sách user (chỉ admin)
app.get("/api/users", async (req, res) => {
    try {
        const users = await User.find({}, "-password"); // ẩn password
        res.json(users);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

//// ===================================================

// Chạy server
const PORT = 3000;
app.listen(PORT, () => console.log(`🚀 Server chạy ở http://localhost:${PORT}`));
