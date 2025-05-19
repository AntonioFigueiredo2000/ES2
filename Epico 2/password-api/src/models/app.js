const mongoose = require('../config/database');
const { v4: uuidv4 } = require('uuid');

const appSchema = new mongoose.Schema({
  appid: { type: String, default: uuidv4, unique: true },
  name: { type: String, required: true },
  owner: { type: String, required: true },
  editors: [{ type: String }],
});

module.exports = mongoose.model('App', appSchema);