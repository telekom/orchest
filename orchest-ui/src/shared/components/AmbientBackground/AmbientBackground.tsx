/** Fixed ambient orb background + grain overlay. Mount once at app root. */
export function AmbientBackground() {
  return (
    <>
      <div className="ambient-canvas" aria-hidden="true" />
      <div className="ambient-grain" aria-hidden="true" />
    </>
  );
}
