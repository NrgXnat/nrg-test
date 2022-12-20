package org.nrg.testing.enums

import ij.ImagePlus
import ij.ImageStack
import ij.io.Opener
import loci.formats.FormatException
import loci.formats.FormatReader
import loci.formats.in.DicomReader
import loci.formats.in.NiftiReader
import loci.plugins.util.ImageProcessorReader

enum ImageType {

    DICOM {
        @Override
        ImagePlus readImage(File image) throws IOException, FormatException {
            final DicomReader dicomReader = new DicomReader()
            dicomReader.setGroupFiles(false) // don't want to try to pull in other files
            readWith(image, dicomReader)
        }
    },

    NIFTI {
        @Override
        ImagePlus readImage(File image) throws IOException, FormatException {
            readWith(image, new NiftiReader())
        }
    },

    PLAIN_IMAGE {
        @Override
        ImagePlus readImage(File image) throws IOException, FormatException {
            new Opener().openImage(image.getPath())
        }
    }

    protected ImagePlus readWith(File image, FormatReader reader) throws IOException, FormatException {
        reader.setId(image.path)
        final ImageProcessorReader processorReader = new ImageProcessorReader(reader)
        final ImageStack imageStack = new ImageStack(reader.sizeX, reader.sizeY)
        (0 ..< reader.sizeZ).each { z ->
            imageStack.addSlice(processorReader.openProcessors(z)[0]) // add each slice to stack
        }
        reader.close()
        processorReader.close()
        new ImagePlus(image.name, imageStack)
    }

    abstract ImagePlus readImage(File image) throws IOException, FormatException

}
