/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#f0f7ff',
          100: '#dceaff',
          500: '#1e6dd6',
          600: '#1559b8',
          700: '#114792',
        },
      },
    },
  },
  plugins: [],
};
