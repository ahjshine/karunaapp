import { useEffect, useRef } from "react"
import * as THREE from "three"

export function ThreeDnaCanvas() {
  const mountRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const container = mountRef.current
    if (!container) return

    const width = container.clientWidth || 600
    const height = container.clientHeight || 500

    const scene = new THREE.Scene()
    const camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000)
    camera.position.set(0, 0, 34)

    const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true })
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
    renderer.setSize(width, height)
    container.appendChild(renderer.domElement)

    // Lighting
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.85)
    scene.add(ambientLight)

    const pointLight1 = new THREE.PointLight(0x00f2fe, 3.5, 60)
    pointLight1.position.set(12, 16, 18)
    scene.add(pointLight1)

    const pointLight2 = new THREE.PointLight(0xa855f7, 2.8, 50)
    pointLight2.position.set(-14, -14, 14)
    scene.add(pointLight2)

    const pointLight3 = new THREE.PointLight(0xf43f5e, 2.5, 50)
    pointLight3.position.set(0, 15, -10)
    scene.add(pointLight3)

    // Master DNA Group
    const systemGroup = new THREE.Group()
    scene.add(systemGroup)

    const numRungs = 48
    const heightSpan = 26
    const radius = 4.8
    const totalTurns = 2.8

    const sectionPalette = [
      { name: "Cranial Telemetry", color: 0x00f2fe, emissive: 0x00586e, lineCol: 0x38bdf8 },
      { name: "Cardiac Vitals", color: 0x10b981, emissive: 0x044e34, lineCol: 0x34d399 },
      { name: "Genomic Core", color: 0xf59e0b, emissive: 0x78350f, lineCol: 0xfcd34d },
      { name: "Threat Anomaly", color: 0xf43f5e, emissive: 0x881337, lineCol: 0xfb7185 },
      { name: "Cryptographic Vault", color: 0xa855f7, emissive: 0x581c87, lineCol: 0xc084fc },
    ]

    function getSection(tNorm: number) {
      const idx = Math.min(Math.floor(tNorm * sectionPalette.length), sectionPalette.length - 1)
      return sectionPalette[idx]
    }

    const sphereGeo = new THREE.SphereGeometry(0.22, 14, 14)
    const rungsGroup = new THREE.Group()
    const backboneGroup = new THREE.Group()
    systemGroup.add(rungsGroup)
    systemGroup.add(backboneGroup)

    for (let i = 0; i < numRungs; i++) {
      const t = i / (numRungs - 1)
      const angle = t * Math.PI * 2 * totalTurns
      const y = (t - 0.5) * heightSpan

      const x1 = Math.cos(angle) * radius
      const z1 = Math.sin(angle) * radius

      const x2 = Math.cos(angle + Math.PI) * radius
      const z2 = Math.sin(angle + Math.PI) * radius

      const p1 = new THREE.Vector3(x1, y, z1)
      const p2 = new THREE.Vector3(x2, y, z2)

      const section = getSection(t)

      // Node 1
      const mat1 = new THREE.MeshPhongMaterial({
        color: section.color,
        emissive: section.emissive,
        shininess: 90,
      })
      const node1 = new THREE.Mesh(sphereGeo, mat1)
      node1.position.copy(p1)
      backboneGroup.add(node1)

      // Node 2
      const mat2 = new THREE.MeshPhongMaterial({
        color: section.color,
        emissive: section.emissive,
        shininess: 90,
      })
      const node2 = new THREE.Mesh(sphereGeo, mat2)
      node2.position.copy(p2)
      backboneGroup.add(node2)

      // Rung line
      const midPoint = new THREE.Vector3().addVectors(p1, p2).multiplyScalar(0.5)
      const rungPoints = [p1, midPoint, p2]
      const rungGeo = new THREE.BufferGeometry().setFromPoints(rungPoints)
      const rungMat = new THREE.LineBasicMaterial({
        color: section.lineCol,
        transparent: true,
        opacity: 0.75,
      })
      const rungLine = new THREE.Line(rungGeo, rungMat)
      rungsGroup.add(rungLine)
    }

    // 3 Hologram Diagnostic Orbit Rings with Satellites
    const orbitalRings: { group: THREE.Group; sat: THREE.Mesh; radius: number; speed: number }[] = []
    for (let r = 0; r < 3; r++) {
      const orbitGroup = new THREE.Group()
      const orbitRadius = 7.5 + r * 2.2
      const curve = new THREE.EllipseCurve(0, 0, orbitRadius, orbitRadius * 0.9, 0, 2 * Math.PI, false, 0)
      const points = curve.getPoints(64)
      const orbitGeo = new THREE.BufferGeometry().setFromPoints(points.map((p) => new THREE.Vector3(p.x, 0, p.y)))

      const ringCol = [0x00f2fe, 0xa855f7, 0xf59e0b][r % 3]
      const orbitMat = new THREE.LineBasicMaterial({
        color: ringCol,
        transparent: true,
        opacity: 0.28 + r * 0.1,
      })
      const ringMesh = new THREE.Line(orbitGeo, orbitMat)

      orbitGroup.rotation.x = THREE.MathUtils.degToRad(65 + r * 22)
      orbitGroup.rotation.y = THREE.MathUtils.degToRad(r * 30)
      orbitGroup.position.y = (r - 1) * 6
      orbitGroup.add(ringMesh)

      const satGeo = new THREE.SphereGeometry(0.24, 10, 10)
      const satMat = new THREE.MeshBasicMaterial({ color: ringCol })
      const satMesh = new THREE.Mesh(satGeo, satMat)
      orbitGroup.add(satMesh)

      systemGroup.add(orbitGroup)
      orbitalRings.push({ group: orbitGroup, sat: satMesh, radius: orbitRadius, speed: 0.8 + r * 0.4 })
    }

    // Floating Data Particles
    const particleCount = 180
    const particlePositions: number[] = []
    const particleColors: number[] = []

    for (let i = 0; i < particleCount; i++) {
      const theta = Math.random() * Math.PI * 2
      const dist = 2.0 + Math.random() * 8.5
      const py = (Math.random() - 0.5) * heightSpan * 1.15
      const px = Math.cos(theta) * dist
      const pz = Math.sin(theta) * dist

      particlePositions.push(px, py, pz)

      const tNorm = py / (heightSpan * 1.15) + 0.5
      const colObj = new THREE.Color(getSection(Math.max(0, Math.min(1, tNorm))).color)
      particleColors.push(colObj.r, colObj.g, colObj.b)
    }

    const particleGeo = new THREE.BufferGeometry()
    particleGeo.setAttribute("position", new THREE.Float32BufferAttribute(particlePositions, 3))
    particleGeo.setAttribute("color", new THREE.Float32BufferAttribute(particleColors, 3))

    const particleMat = new THREE.PointsMaterial({
      size: 0.15,
      vertexColors: true,
      transparent: true,
      opacity: 0.65,
      blending: THREE.AdditiveBlending,
    })

    const particles = new THREE.Points(particleGeo, particleMat)
    systemGroup.add(particles)

    // Initial axis tilt
    systemGroup.rotation.z = THREE.MathUtils.degToRad(-12)
    systemGroup.rotation.x = THREE.MathUtils.degToRad(10)

    // Interactive mouse parallax
    let targetRotY = 0
    let targetRotX = 0

    const handleMouseMove = (event: MouseEvent) => {
      const mouseX = (event.clientX / window.innerWidth) * 2 - 1
      const mouseY = -(event.clientY / window.innerHeight) * 2 + 1
      targetRotY = mouseX * 0.35
      targetRotX = mouseY * 0.2
    }
    window.addEventListener("mousemove", handleMouseMove)

    const handleResize = () => {
      if (!container) return
      const w = container.clientWidth || window.innerWidth
      const h = container.clientHeight || window.innerHeight
      camera.aspect = w / h
      camera.updateProjectionMatrix()
      renderer.setSize(w, h)
    }
    window.addEventListener("resize", handleResize)

    const clock = new THREE.Clock()
    let animationFrameId: number

    const animate = () => {
      animationFrameId = requestAnimationFrame(animate)
      const delta = clock.getDelta()
      const elapsed = clock.getElapsedTime()

      rungsGroup.rotation.y += delta * 0.55
      backboneGroup.rotation.y += delta * 0.55
      particles.rotation.y += delta * 0.2

      orbitalRings.forEach((ring, idx) => {
        const angle = elapsed * ring.speed * (idx % 2 === 0 ? 1 : -1)
        ring.sat.position.set(Math.cos(angle) * ring.radius, 0, Math.sin(angle) * ring.radius * 0.9)
        ring.group.rotation.z += delta * 0.15
      })

      systemGroup.rotation.y += (targetRotY - systemGroup.rotation.y) * 0.05
      systemGroup.rotation.x += (targetRotX + THREE.MathUtils.degToRad(10) - systemGroup.rotation.x) * 0.05
      systemGroup.position.y = Math.sin(elapsed * 1.2) * 0.35

      renderer.render(scene, camera)
    }
    animate()

    return () => {
      cancelAnimationFrame(animationFrameId)
      window.removeEventListener("mousemove", handleMouseMove)
      window.removeEventListener("resize", handleResize)
      renderer.dispose()
      if (renderer.domElement && container.contains(renderer.domElement)) {
        container.removeChild(renderer.domElement)
      }
    }
  }, [])

  return <div ref={mountRef} className="h-full w-full" />
}
