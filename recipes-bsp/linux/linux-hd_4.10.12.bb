DESCRIPTION = "Linux kernel for ${MACHINE}"
SECTION = "kernel"
LICENSE = "GPLv2"

PACKAGE_ARCH = "${MACHINE_ARCH}"

KERNEL_RELEASE = "4.10.12"

COMPATIBLE_MACHINE = "hd11|hd51|hd500c|hd530c|hd1100|hd1200|hd1265|hd1500|hd2400|vs1000|vs1500|bre2ze4k|axultra"

inherit kernel machine_kernel_pr

SRC_URI[mips.md5sum] = "3c42df14db9d12041802f4c8fec88e17"
SRC_URI[mips.sha256sum] = "738896d2682211d2079eeaa1c7b8bdd0fe75eb90cd12dff2fc5aeb3cc02562bc"
SRC_URI[arm.md5sum] = "bda1c09ed92a805cedc6770c0dd40e81"
SRC_URI[arm.sha256sum] = "67a3ac98727595a399d5c399d3b66a7fadbe8136ac517e08decba5ea6964674a"

LIC_FILES_CHKSUM = "file://${WORKDIR}/linux-${PV}/COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

# By default, kernel.bbclass modifies package names to allow multiple kernels
# to be installed in parallel. We revert this change and rprovide the versioned
# package names instead, to allow only one kernel to be installed.
PKG_kernel-base = "kernel-base"
PKG_kernel-image = "kernel-image"
RPROVIDES_kernel-base = "kernel-${KERNEL_VERSION}"
RPROVIDES_kernel-image = "kernel-image-${KERNEL_VERSION}"

SRC_URI += "http://downloads.mutant-digital.net/linux-${PV}-${ARCH}.tar.gz;name=${ARCH} \
    file://defconfig \
    file://initramfs-subdirboot.cpio.gz;unpack=0 \
    file://export_pmpoweroffprepare.patch \
    file://TBS-fixes-for-4.10-kernel.patch \
    file://0001-Support-TBS-USB-drivers-for-4.6-kernel.patch \
    file://0001-TBS-fixes-for-4.6-kernel.patch \
    file://0001-STV-Add-PLS-support.patch \
    file://0001-STV-Add-SNR-Signal-report-parameters.patch \
    file://blindscan2.patch \
    file://0001-stv090x-optimized-TS-sync-control.patch \
    file://t230c2.patch \
    file://add-more-devices-rtl8xxxu.patch \
    "

SRC_URI_append_arm = " \
    file://findkerneldevice.sh \
    file://reserve_dvb_adapter_0.patch \
    file://export_pmpoweroffprepare.patch \
    file://enable_hauppauge_solohd.patch \
    "

S = "${WORKDIR}/linux-${PV}"

export OS = "Linux"
KERNEL_OBJECT_SUFFIX = "ko"

# MIPSEL
KERNEL_IMAGETYPE_mipsel = "vmlinux.gz"
KERNEL_OUTPUT_DIR_mipsel = "."
KERNEL_IMAGEDEST_mipsel = "tmp"
KERNEL_CONSOLE_mipsel = "null"
SERIAL_CONSOLE_mipsel ?= ""

# Replaced by kernel_output_dir
KERNEL_OUTPUT_mipsel = "vmlinux.gz"

pkg_postinst_kernel-image_mipsel() {
	if [ "x$D" == "x" ]; then
		if [ -f /${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE} ] ; then
			flash_eraseall /dev/mtd1
			nandwrite -p /dev/mtd1 /${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}
		fi
	fi
	true
}

# ARM
KERNEL_OUTPUT_arm = "arch/${ARCH}/boot/${KERNEL_IMAGETYPE}"
KERNEL_IMAGETYPE_arm = "zImage"
KERNEL_IMAGEDEST_arm = "tmp"

FILES_kernel-image_arm = "/${KERNEL_IMAGEDEST}/findkerneldevice.sh"

kernel_do_configure_prepend_arm() {
	install -d ${B}/usr
	install -m 0644 ${WORKDIR}/initramfs-subdirboot.cpio.gz ${B}/
}

kernel_do_install_append_arm() {
	install -d ${D}/${KERNEL_IMAGEDEST}
	install -m 0755 ${WORKDIR}/findkerneldevice.sh ${D}/${KERNEL_IMAGEDEST}
}

pkg_postinst_kernel-image_arm() {
	if [ "x$D" == "x" ]; then
		if [ -f /${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE} ] ; then
			/${KERNEL_IMAGEDEST}/./findkerneldevice.sh
			dd if=/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE} of=/dev/kernel
		fi
	fi
    true
}
