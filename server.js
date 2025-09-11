// server.js
const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const multer = require("multer");

const app = express();

// Middleware
app.use(express.json());
app.use(cors());

// 🔑 SECRET KEY cho JWT
const JWT_SECRET = "secret123";

// Kết nối MongoDB
mongoose.connect(
  "mongodb+srv://bdx:123456789%40@cluster0.xmmbfgf.mongodb.net/qlsukien?retryWrites=true&w=majority",
  {
    useNewUrlParser: true,
    useUnifiedTopology: true
  }
).then(() => console.log("✅ MongoDB connected"))
  .catch(err => console.log("❌ MongoDB connection error:", err));

// Schema và Model cho người dùng
const userSchema = new mongoose.Schema({
  username: { type: String, required: true, unique: true },
  fullName: { type: String },
  email: { type: String, required: true, unique: true },
  phone: { type: String, required: true },
  password: { type: String, required: true },
  avatar: { type: String }, // 👈 thêm avatar (base64)
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

const User = mongoose.model("User", userSchema);

// ⚡ Cấu hình multer (upload vào RAM, không lưu file)
const storage = multer.memoryStorage();
const upload = multer({ storage });

// ==================== API ====================

// API đăng ký
app.post("/register", async (req, res) => {
  try {
    const { username, fullName, email, phone, password } = req.body;

    if (!username || !fullName || !email || !phone || !password) {
      return res.status(400).json({ message: "Vui lòng điền đầy đủ thông tin." });
    }

    // Validate email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return res.status(400).json({ message: "Email không hợp lệ." });
    }

    // Validate số điện thoại
    const phoneRegex = /^[0-9]{9,12}$/;
    if (!phoneRegex.test(phone)) {
      return res.status(400).json({ message: "Số điện thoại không hợp lệ." });
    }

    // Kiểm tra trùng username hoặc email
    const existingUser = await User.findOne({
      $or: [{ username }, { email }]
    });
    if (existingUser) {
      return res.status(400).json({ message: "Tên đăng nhập hoặc email đã tồn tại." });
    }

    // Mã hóa mật khẩu
    const hashedPassword = await bcrypt.hash(password, 10);

    const newUser = new User({
      username,
      fullName,
      email,
      phone,
      password: hashedPassword,
      createdAt: new Date(),
      updatedAt: new Date()
    });

    await newUser.save();

    return res.status(201).json({ message: "Đăng ký thành công!" });
  } catch (err) {
    console.error("❌ Lỗi /register:", err);
    return res.status(500).json({ message: "Lỗi server." });
  }
});

// API đăng nhập
app.post("/login", async (req, res) => {
  const { username, password } = req.body;

  if (!username || !password) {
    return res.status(400).json({ message: "Vui lòng nhập đầy đủ thông tin." });
  }

  try {
    const user = await User.findOne({ username });
    if (!user) {
      return res.status(400).json({ message: "Tài khoản không tồn tại." });
    }

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res.status(400).json({ message: "Sai mật khẩu." });
    }

    const token = jwt.sign({ id: user._id }, JWT_SECRET, { expiresIn: "7d" });

    return res.json({
      success: true,
      message: "Đăng nhập thành công!",
      token,
      user: {
        id: user._id,
        username: user.username,
        email: user.email,
        phone: user.phone,
        avatar: user.avatar || null
      }
    });
  } catch (err) {
    console.error("❌ Lỗi /login:", err);
    return res.status(500).json({ message: "Lỗi server." });
  }
});

// Lấy danh sách nhân viên
app.get("/nhanvien", async (req, res) => {
  try {
    const nhanvien = await User.find();
    res.json(nhanvien);
  } catch (err) {
    res.status(500).json({ message: "Lỗi server" });
  }
});

// API cập nhật thông tin nhân viên
app.put("/api/updateNhanVien", async (req, res) => {
  try {
    const { username, fullName, email, phone } = req.body;

    if (!username) {
      return res.status(400).json({ message: "Thiếu username." });
    }

    const updatedUser = await User.findOneAndUpdate(
      { username },
      { fullName, email, phone, updatedAt: new Date() },
      { new: true }
    );

    if (!updatedUser) {
      return res.status(404).json({ message: "Nhân viên không tồn tại." });
    }

    res.json({ message: "Cập nhật thông tin thành công!", user: updatedUser });
  } catch (err) {
    console.error("❌ Lỗi /api/updateNhanVien:", err);
    res.status(500).json({ message: "Lỗi server." });
  }
});

// API đổi mật khẩu
app.put("/api/changePassword", async (req, res) => {
  try {
    const { username, newPassword } = req.body;

    if (!username || !newPassword) {
      return res.status(400).json({ message: "Thiếu username hoặc mật khẩu mới." });
    }

    const hashedPassword = await bcrypt.hash(newPassword, 10);

    const updatedUser = await User.findOneAndUpdate(
      { username },
      { password: hashedPassword, updatedAt: new Date() },
      { new: true }
    );

    if (!updatedUser) {
      return res.status(404).json({ message: "Nhân viên không tồn tại." });
    }

    res.json({ message: "Đổi mật khẩu thành công!" });
  } catch (err) {
    console.error("❌ Lỗi /api/changePassword:", err);
    res.status(500).json({ message: "Lỗi server." });
  }
});

// API cập nhật avatar
app.post("/api/updateAvatar", upload.single("avatarFile"), async (req, res) => {
  try {
    const { username } = req.body;
    const file = req.file;

    if (!username || !file) {
      return res.status(400).json({ message: "Thiếu username hoặc file." });
    }

    // Chuyển file thành base64
    const avatarBase64 = file.buffer.toString("base64");

    const updatedUser = await User.findOneAndUpdate(
      { username },
      { avatar: avatarBase64, updatedAt: new Date() },
      { new: true }
    );

    if (!updatedUser) {
      return res.status(404).json({ message: "Nhân viên không tồn tại." });
    }

    res.json({
      message: "Cập nhật avatar thành công!",
      avatar: updatedUser.avatar
    });
  } catch (err) {
    console.error("❌ Lỗi /api/updateAvatar:", err);
    res.status(500).json({ message: "Lỗi server." });
  }
});

// Server chạy trên port 5000
app.listen(5000, () => {
  console.log("🚀 Server running on http://localhost:5000");
});
