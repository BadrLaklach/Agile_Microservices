import type { Config } from 'tailwindcss';

const config: Config = {
  darkMode: ["class"],
  content: [
    './pages/**/*.{ts,tsx}',
    './components/**/*.{ts,tsx}',
    './app/**/*.{ts,tsx}',
    './src/**/*.{ts,tsx}',
    './index.html',
  ],
  theme: {
    extend: {
      colors: {
        // Core Enterprise Mapping
        border: "rgba(79, 37, 46, 0.15)", // Premium subtle grounding border
        input: "rgba(79, 37, 46, 0.1)",
        ring: "#4F252E",
        background: "#FFFFFF",
        foreground: "#4F252E", // High-contrast structural text
        
        // Brand Color Specifics
        brand: {
          burgundy: "#4F252E",  // Main structural tone, deep text, dominant headers
          mint: "#C1EBE9",      // Secondary workspace surfaces, tabs, active indicators
          amber: "#F4AE52",     // Critical focus states, alert banners, milestone highlights
          straw: "#FFF7C5",     // High-density wells, alternating rows, card grouping panels
        },

        // Semantic Mapping for Shadcn Primitives
        primary: {
          DEFAULT: "#4F252E",
          foreground: "#FFF7C5",
        },
        secondary: {
          DEFAULT: "#C1EBE9",
          foreground: "#4F252E",
        },
        destructive: {
          DEFAULT: "#B91C1C",
          foreground: "#FFFFFF",
        },
        muted: {
          DEFAULT: "rgba(255, 247, 197, 0.35)", // Ghost tint of straw for table zebra striping
          foreground: "rgba(79, 37, 46, 0.7)",
        },
        accent: {
          DEFAULT: "#F4AE52",
          foreground: "#4F252E",
        },
        popover: {
          DEFAULT: "#FFFFFF",
          foreground: "#4F252E",
        },
        card: {
          DEFAULT: "#FFFFFF",
          foreground: "#4F252E",
        },
      },
      borderRadius: {
        // The 'Crisp' UI definition: Avoids modern bubble shapes (md/lg/xl) to match classic robust toolings
        lg: "6px",
        md: "4px",
        sm: "2px",
        DEFAULT: "4px",
      },
      boxShadow: {
        // Rigid enterprise isometric shadow offsets instead of soft blurred floating filters
        'crisp-sm': '1px 1px 0px 0px rgba(79, 37, 46, 0.15)',
        'crisp-md': '2px 2px 0px 0px rgba(79, 37, 46, 1)',
        'crisp-amber': '2px 2px 0px 0px #F4AE52',
        'crisp-active': '0px 0px 0px 0px transparent',
      }
    },
  },
  plugins: [require("tailwindcss-animate")],
};

export default config;
