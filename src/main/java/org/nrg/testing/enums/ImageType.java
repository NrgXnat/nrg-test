package org.nrg.testing.enums;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.in.DicomReader;
import loci.formats.in.NiftiReader;
import loci.plugins.util.ImageProcessorReader;

import java.io.File;
import java.io.IOException;

public enum ImageType {

    DICOM {
        @Override
        public ImagePlus readImage(File image) throws IOException, FormatException {
            return readWith(image, new DicomReader());
        }
    },

    NIFTI {
        @Override
        public ImagePlus readImage(File image) throws IOException, FormatException {
            return ImageType.readWith(image, new NiftiReader());
        }
    },

    PLAIN_IMAGE {
        @Override
        public ImagePlus readImage(File image) throws IOException, FormatException {
            final Opener imageOpener = new Opener();
            return imageOpener.openImage(image.getPath());
        }
    };

    private static ImagePlus readWith(File image, FormatReader reader) throws IOException, FormatException {
        reader.setId(image.getPath());
        final ImageProcessorReader processorReader = new ImageProcessorReader(reader);
        final ImageStack imageStack = new ImageStack(reader.getSizeX(), reader.getSizeY());
        for (int z = 0; z < reader.getSizeZ(); z++) {
            imageStack.addSlice(processorReader.openProcessors(z)[0]); // add each slice to stack
        }
        reader.close();
        processorReader.close();
        return new ImagePlus(image.getName(), imageStack);
    }

    public abstract ImagePlus readImage(File image) throws IOException, FormatException;

}
