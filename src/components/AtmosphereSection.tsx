export function AtmosphereSection() {
  return (
    <section id="hospitals" className="relative min-h-[640px] overflow-hidden bg-[#0b1220] py-28 text-white lg:min-h-[720px]">
      {/* Background Nurse Corridor Image */}
      <div className="absolute inset-0 z-0">
        <img
          src="/assets/3c7bc.png"
          alt="A nurse moving through a hospital corridor with clinical equipment"
          className="h-full w-full object-cover object-center filter brightness-90 contrast-105"
        />
        {/* Cinematic Gradient Overlays */}
        <div className="absolute inset-0 bg-gradient-to-r from-[#040d15]/95 via-[#040d15]/75 to-[#040d15]/90" />
        <div className="absolute inset-0 bg-gradient-to-t from-[#040d15] via-transparent to-[#040d15]/60" />
      </div>

      <div className="relative z-10 mx-auto flex h-full max-w-7xl flex-col justify-center px-4 sm:px-6 lg:px-8">
        <div className="max-w-2xl">
          <div className="font-mono text-xs font-bold tracking-widest text-[#12f5d0] uppercase">
            Karuna understands
          </div>

          <h2 className="mt-4 text-3xl font-light tracking-tight text-white sm:text-5xl lg:text-6xl">
            respect clinical workflows,{" "}
            <span className="font-serif italic font-normal text-[#12f5d0]">protect patients privacy.</span>
          </h2>

          <p className="mt-6 text-base leading-relaxed text-[#d3dddc] sm:text-xl">
            Healthcare teams need security that recognizes the difference between everyday clinical work and activity
            that deserves closer examination.
          </p>

          <div className="mt-8 flex flex-wrap gap-4 text-xs font-mono text-slate-300">
            <div className="rounded-lg border border-white/10 bg-white/5 px-3.5 py-2 backdrop-blur-md">
              0% Friction on Care Teams
            </div>
            <div className="rounded-lg border border-white/10 bg-white/5 px-3.5 py-2 backdrop-blur-md">
              Sub-second Threat Scoring
            </div>
            <div className="rounded-lg border border-white/10 bg-white/5 px-3.5 py-2 backdrop-blur-md">
              Universal EHR Compatibility
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
