import type { Metadata } from "next";
import { Manrope, Playfair_Display } from "next/font/google";

import { NavBar } from "@/components/nav-bar";

import "./globals.css";

const sans = Manrope({
  subsets: ["latin"],
  variable: "--font-sans",
});

const serif = Playfair_Display({
  subsets: ["latin"],
  variable: "--font-serif",
});

export const metadata: Metadata = {
  title: "Revy Service App",
  description: "서비스 API 기반 고객용 스캐폴딩 앱",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className={`${sans.variable} ${serif.variable}`}>
        <NavBar />
        {children}
      </body>
    </html>
  );
}
