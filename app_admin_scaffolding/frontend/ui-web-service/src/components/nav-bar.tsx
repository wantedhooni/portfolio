import Link from "next/link";

export function NavBar() {
  return (
    <header className="nav">
      <Link className="brand" href="/">
        <span className="brand-mark">RS</span>
        <span className="brand-copy">
          <strong>Revy Service</strong>
          <span>customer access layer</span>
        </span>
      </Link>
      <nav className="nav-links">
        <Link href="/signup">Signup</Link>
        <Link href="/signin">Signin</Link>
      </nav>
    </header>
  );
}
