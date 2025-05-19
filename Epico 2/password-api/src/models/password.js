const mongoose = require('../config/database');

const passwordSchema = new mongoose.Schema({
  appid: { type: String, required: true },
  password: { type: String, required: true },
  createdBy: { type: String, required: true },
  createdAt: { type: Date, default: Date.now },
});

module.exports = mongoose.model('Password', passwordSchema);