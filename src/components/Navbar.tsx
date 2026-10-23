import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"
import { ArrowRight, Menu, Shield, Lock } from "lucide-react"

interface NavbarProps {
  onOpenDemo: () => void
  onOpenLogin: () => void
}

export function Navbar({ onOpenDemo, onOpenLogin }: NavbarProps) {
  const [isOpen, setIsOpen] = useState(false)

  const scrollTo = (id: string) => {
    setIsOpen(false)
    const element = document.getElementById(id)
    if (element) {
      element.scrollIntoView({ behavior: "smooth" })
    }
  }

  return (
    <header className="sticky top-0 z-50 w-full border-b border-[#0b1220]/10 bg-[#c9d8d5]/90 backdrop-blur-md transition-all">
      <div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Logo */}
        <button
          type="button"
          aria-label="Return to the top of the page"
          onClick={() => {
            window.scrollTo({ top: 0, behavior: "smooth" })
          }}
          className="group flex items-center gap-3 bg-transparent p-0 text-left transition-opacity hover:opacity-90"
        >
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#0b1220] shadow-sm transition-transform group-hover:scale-105">
            <Shield className="h-5 w-5 text-[#14b8a6]" />
          </span>
          <span className="flex items-center gap-3">
            <span className="text-2xl font-bold tracking-tight text-[#0b1220]">
              Karuna<span className="text-[#14b8a6]">.</span>
            </span>
            <span className="hidden h-7 w-px bg-[#a9bfbb] sm:block" />
            <span className="hidden flex-col text-[10px] font-semibold tracking-wider text-[#496159] uppercase sm:flex">
              <span>Clinical Access</span>
              <span>Intelligence</span>
            </span>
          </span>
        </button>

        {/* Desktop Navigation */}
        <nav className="hidden items-center gap-8 md:flex">
          <button
            onClick={() => scrollTo("platform")}
            className="text-sm font-medium text-[#344b49] transition-colors hover:text-[#0b1220]"
          >
            Platform
          </button>
          <button
            onClick={() => scrollTo("how-it-works")}
            className="text-sm font-medium text-[#344b49] transition-colors hover:text-[#0b1220]"
          >
            How It Works
          </button>
          <button
            onClick={() => scrollTo("hospitals")}
            className="text-sm font-medium text-[#344b49] transition-colors hover:text-[#0b1220]"
          >
            For Hospitals
          </button>
          <button
            onClick={() => scrollTo("security")}
            className="text-sm font-medium text-[#344b49] transition-colors hover:text-[#0b1220]"
          >
            Security
          </button>
        </nav>

        {/* Right CTA */}
        <div className="hidden items-center gap-3 md:flex">
          <Button
            variant="ghost"
            size="sm"
            onClick={onOpenLogin}
            className="gap-1.5 text-xs font-semibold text-[#0d514f] hover:bg-[#0b1220]/10 hover:text-[#0b1220]"
          >
            <Lock className="h-3.5 w-3.5 text-[#14b8a6]" />
            <span>Staff Portal</span>
          </Button>

          <Button
            onClick={onOpenDemo}
            className="group gap-2 bg-[#14b8a6] px-5 font-semibold text-[#0b1220] shadow-sm transition-all hover:bg-[#0d9488] hover:text-white"
          >
            Request a Demo
            <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
          </Button>
        </div>

        {/* Mobile Menu */}
        <div className="flex items-center gap-2 md:hidden">
          <Button
            onClick={onOpenLogin}
            size="sm"
            variant="ghost"
            className="text-xs font-semibold text-[#0d514f]"
          >
            Login
          </Button>
          <Button
            onClick={onOpenDemo}
            size="sm"
            className="bg-[#14b8a6] text-xs font-semibold text-[#0b1220] hover:bg-[#0d9488] hover:text-white"
          >
            Demo
          </Button>
          <Sheet open={isOpen} onOpenChange={setIsOpen}>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="text-[#0b1220]">
                <Menu className="h-6 w-6" />
                <span className="sr-only">Toggle navigation menu</span>
              </Button>
            </SheetTrigger>
            <SheetContent side="right" className="bg-[#f6f7f4] text-[#0b1220]">
              <SheetHeader>
                <SheetTitle className="flex items-center gap-2 text-left text-xl font-bold">
                  <Shield className="h-5 w-5 text-[#14b8a6]" />
                  Karuna<span className="text-[#14b8a6]">.</span>
                </SheetTitle>
              </SheetHeader>
              <div className="mt-8 flex flex-col gap-5">
                <button
                  onClick={() => scrollTo("platform")}
                  className="flex items-center justify-between py-2 text-left text-lg font-medium text-[#0b1220] hover:text-teal-600"
                >
                  Platform
                  <ArrowRight className="h-4 w-4 text-gray-400" />
                </button>
                <button
                  onClick={() => scrollTo("how-it-works")}
                  className="flex items-center justify-between py-2 text-left text-lg font-medium text-[#0b1220] hover:text-teal-600"
                >
                  How It Works
                  <ArrowRight className="h-4 w-4 text-gray-400" />
                </button>
                <button
                  onClick={() => scrollTo("hospitals")}
                  className="flex items-center justify-between py-2 text-left text-lg font-medium text-[#0b1220] hover:text-teal-600"
                >
                  For Hospitals
                  <ArrowRight className="h-4 w-4 text-gray-400" />
                </button>
                <button
                  onClick={() => scrollTo("security")}
                  className="flex items-center justify-between py-2 text-left text-lg font-medium text-[#0b1220] hover:text-teal-600"
                >
                  Security
                  <ArrowRight className="h-4 w-4 text-gray-400" />
                </button>

                <div className="mt-6 flex flex-col gap-3 border-t border-gray-200 pt-6">
                  <Button
                    variant="outline"
                    onClick={() => {
                      setIsOpen(false)
                      onOpenLogin()
                    }}
                    className="w-full gap-2 border-slate-300 font-semibold text-[#0b1220]"
                  >
                    <Lock className="h-4 w-4 text-[#14b8a6]" />
                    <span>Staff Portal Access</span>
                  </Button>
                  <Button
                    onClick={() => {
                      setIsOpen(false)
                      onOpenDemo()
                    }}
                    className="w-full bg-[#14b8a6] font-semibold text-[#0b1220] hover:bg-[#0d9488] hover:text-white"
                  >
                    Request a Demo
                  </Button>
                </div>
              </div>
            </SheetContent>
          </Sheet>
        </div>
      </div>
    </header>
  )
}
