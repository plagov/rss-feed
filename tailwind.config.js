module.exports = {
  content: ["./src/main/resources/templates/**/*.html"],
  theme: {
    extend: {},
  },
  plugins: [],
  minify: process.env.NODE_ENV === 'production'
}
