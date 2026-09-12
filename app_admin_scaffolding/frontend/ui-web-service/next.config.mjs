/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  allowedDevOrigins: ["127.0.0.1", "localhost", "0.0.0.0", "[::1]"],
  distDir: process.env.NEXT_E2E === "true" ? ".next-e2e" : ".next",
};

export default nextConfig;
