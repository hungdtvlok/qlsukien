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
    { useNewUrlParser: true, useUnifiedTopology: true }
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
    avatar: { type: String, default: "" }, // thêm avatar
    createdAt: { type: Date, default: Date.now },
    updatedAt: { type: Date, default: Date.now }
});


const User = mongoose.model("User", userSchema);



// ================== API REGISTER ==================
app.post("/api/register", async (req, res) => {
    try {
        console.log("Body received:", req.body); // log JSON Android gửi
        const { username, fullName, email, phone, password } = req.body;

        // Kiểm tra dữ liệu bắt buộc
        if (!username || !password || !email || !phone) {
            return res.status(400).json({ message: "Thông tin đăng ký không hợp lệ" });
        }

        // Kiểm tra trùng username/email
        const existingUser = await User.findOne({ $or: [{ username }, { email }] });
        if (existingUser) {
            const field = existingUser.username === username ? "Username" : "Email";
            return res.status(400).json({ message: "Tài khoản hoặc email đã tồn tại" });
        }

        // Hash password
        const hashedPassword = await bcrypt.hash(password, 10);

        // Tạo user mới
        const newUser = new User({
            username,
            fullName,
            email,
            phone,
            password: hashedPassword
        });

        // Lưu vào MongoDB
        await newUser.save();

        res.status(201).json({ message: "Đăng ký thành công" });

    } catch (err) {
        console.error("❌ Lỗi đăng ký:", err);

        // Nếu lỗi duplicate key
        if (err.code === 11000) {
            const field = Object.keys(err.keyValue)[0];
            return res.status(400).json({ message: `${field} đã tồn tại` });
        }

        // Lỗi khác
        res.status(500).json({ message: "Server error: " + err.message });
    }
});


// ================== API LOGIN ==================
app.post("/api/login", async (req, res) => {
    try {
        const { username, password } = req.body;

        if (!username || !password) {
            return res.status(400).json({ message: "Thông tin đăng nhập không hợp lệ" });
        }

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

        // Tạo JWT token (1 giờ)
        const token = jwt.sign({ id: user._id, username: user.username }, JWT_SECRET, { expiresIn: "1h" });

        // Trả về token
        res.json({ message: "Đăng nhập thành công", token });

    } catch (err) {
        console.error("❌ Lỗi đăng nhập:", err);
        res.status(500).json({ message: "Server error: " + err.message });
    }
});
// ================== API LẤY NHÂN VIÊN ==================
app.get("/api/nhanvien", async (req, res) => {
    try {
        const { username } = req.query; // Lấy username từ query param
        let users;

        if (username) {
            // Nếu có username, chỉ trả về user đó
            users = await User.find({ username });
        } else {
            // Nếu không có username, trả về tất cả user
            users = await User.find();
        }

        res.json(users);
    } catch (err) {
        console.error("❌ Lỗi lấy nhân viên:", err);
        res.status(500).json({ message: "Server error: " + err.message });
    }
});

// ================== API SỬA THÔNG TIN NHÂN VIÊN ==================
app.post("/api/updateNhanVien", async (req, res) => {
    try {
        const { username, fullName, email, phone } = req.body;

        if (!username || !fullName || !email || !phone) {
            return res.status(400).json({ message: "Thiếu thông tin cập nhật" });
        }

        const user = await User.findOne({ username });
        if (!user) return res.status(404).json({ message: "Người dùng không tồn tại" });

        // Kiểm tra email trùng (nếu đổi)
        const emailExists = await User.findOne({ email, username: { $ne: username } });
        if (emailExists) return res.status(400).json({ message: "Email đã tồn tại" });

        user.fullName = fullName;
        user.email = email;
        user.phone = phone;
        user.updatedAt = new Date();

        await user.save();
        res.json({ message: "Cập nhật thông tin thành công" });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: "Server error: " + err.message });
    }
});

// ================== API ĐỔI MẬT KHẨU ==================
app.post("/api/changePassword", async (req, res) => {
    try {
        const { username, newPassword } = req.body;

        if (!username || !newPassword) return res.status(400).json({ message: "Thiếu thông tin đổi mật khẩu" });

        const user = await User.findOne({ username });
        if (!user) return res.status(404).json({ message: "Người dùng không tồn tại" });

        const hashedPassword = await bcrypt.hash(newPassword, 10);
        user.password = hashedPassword;
        user.updatedAt = new Date();

        await user.save();
        res.json({ message: "Đổi mật khẩu thành công" });

    } catch (err) {
        console.error(err);
        res.status(500).json({ message: "Server error: " + err.message });
    }
});
// ================== ảnh ==================
app.post("/api/updateAvatar", async (req, res) => {
    try {
        console.log("Body nhận từ client:", req.body);  // <-- thêm log này

        const { username, avatar } = req.body;
        if (!username || !avatar) {
            return res.status(400).json({ message: "Thiếu thông tin avatar" });
        }

        const user = await User.findOne({ username });
        if (!user) {
            return res.status(404).json({ message: "Người dùng không tồn tại" });
        }

        user.avatar = avatar; // Lưu chuỗi base64
        user.updatedAt = new Date();
        await user.save();

        res.json({ message: "Cập nhật avatar thành công", avatar: user.avatar });
    } catch (err) {
        console.error("Lỗi trong updateAvatar:", err);
        res.status(500).json({ message: "Server error: " + err.message });
    }
});



// ================== START SERVER ==================
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`✅ Server running on http://localhost:${PORT}`);
});











