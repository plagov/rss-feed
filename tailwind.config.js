module.exports = {
  content: ["./src/main/resources/templates/**/*.html"],
  theme: {
    extend: {},
  },
  darkMode: 'media',
  plugins: [],
  minify: process.env.NODE_ENV === 'production'
}
