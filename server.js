// ================== IMPORT MODULES ==================
const express = require("express");
const mongoose = require("mongoose");
const bodyParser = require("body-parser");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");

// ================== INIT APP ==================
const app = express();

// Middleware để parse JSON từ client
app.use(bodyParser.json());
// Cho phép CORS để Android app có thể gọi API
app.use(cors());

// 🔑 SECRET KEY cho JWT
const JWT_SECRET = "secret123";

// ================== MONGO DB CONNECTION ==================
mongoose.connect(
    "mongodb+srv://bdx:123456789%40@cluster0.xmmbfgf.mongodb.net/qlsukien?retryWrites=true&w=majority&appName=Cluster0",
    {
        useNewUrlParser: true,
        useUnifiedTopology: true
    }
)
.then(() => console.log("✅ MongoDB connected"))
.catch(err => console.error("❌ MongoDB connection error:", err));

// ================== USER SCHEMA ==================
const userSchema = new mongoose.Schema({
    username: { type: String, required: true, unique: true },
    fullName: { type: String },
    email: { type: String, required: true, unique: true },
    phone: { type: String, required: true },
    password: { type: String, required: true },
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date, default: Date.now }
});

const User = mongoose.model("User", userSchema);

// ================== API REGISTER ==================
app.post("/api/register", async (req, res) => {
    try {
        const { username, fullName, email, phone, password } = req.body;

        // Kiểm tra username hoặc email đã tồn tại chưa
        const existingUser = await User.findOne({ $or: [{ username }, { email }] });
        if (existingUser) {
            return res.status(400).json({ message: "Username hoặc Email đã tồn tại" });
        }

        // Hash password trước khi lưu
        const hashedPassword = await bcrypt.hash(password, 10);

        // Tạo user mới
        const newUser = new User({
            username,
            fullName,
            email,
            phone,
            password: hashedPassword
        });

        await newUser.save(); // Lưu vào DB

        res.status(201).json({ message: "Đăng ký thành công" });
    } catch (err) {
        console.error("❌ Lỗi đăng ký:", err);
        res.status(500).json({ message: "Server error" });
    }
});

// ================== API LOGIN ==================
app.post("/api/login", async (req, res) => {
    try {
        const { username, password } = req.body;

        // Tìm user theo username
        const user = await User.findOne({ username });
        if (!user) {
            return res.status(400).json({ message: "Sai tài khoản hoặc mật khẩu" });
        }

        // Kiểm tra password
        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(400).json({ message: "Sai tài khoản hoặc mật khẩu" });
        }

        // Tạo JWT token
        const token = jwt.sign({ id: user._id, username: user.username }, JWT_SECRET, { expiresIn: "1h" });

        // Trả về token và thông báo
        res.json({ message: "Đăng nhập thành công", token });
    } catch (err) {
        console.error("❌ Lỗi đăng nhập:", err);
        res.status(500).json({ message: "Server error" });
    }
});

// ================== START SERVER ==================
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`✅ Server running on http://localhost:${PORT}`);
});
