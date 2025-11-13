import type { Metadata } from "next";
import "./globals.css";
import Navbar from "./components/navbar";
import { AuthProvider } from "@/context/AuthContext";

export const metadata: Metadata = {
    title: "우리집 물고기",
    description: "물고기 키우기 커뮤니티",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className="bg-gray-50">
        <AuthProvider>
          <Navbar />
          <main className="min-h-screen">
            {children}
          </main>
        </AuthProvider>
      </body>
    </html>
  );
}