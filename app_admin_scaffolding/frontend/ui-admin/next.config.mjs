/** @type {import('next').NextConfig} */
const allowedDevOrigins = [
  "localhost",
  "127.0.0.1",
  "0.0.0.0",
  "[::1]",
  "*.localhost",
  "**.localhost",
  ...(process.env.ALLOWED_DEV_ORIGINS?.split(",").map((origin) => origin.trim()).filter(Boolean) ?? []),
];

const nextConfig = {
  output: "standalone",
  allowedDevOrigins,
};

export default nextConfig;
