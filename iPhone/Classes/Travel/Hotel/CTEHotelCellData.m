//
//  CTEHotelCellData.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CTEHotelCellData.h"
#import "DownloadableUIImage.h"
#import "HotelAnnotation.h"

@interface CTEHotelCellData()

@end

@implementation CTEHotelCellData

-(instancetype)initWithCTEHotel:(CTEHotel *)cteHotel
{
    self = [super init];
    if (!self) {
        return nil;
    }
    self.cellIdentifier = @"HotelSearchTableViewCell";
    self.cellHeight = 130.0 ;
    self.cteHotel = cteHotel ;
    
    if ([cteHotel.thumbnail length]) {
        _downloableImageIcon = [[DownloadableUIImage alloc] init];
        _downloableImageIcon.URL = [NSURL URLWithString:cteHotel.thumbnail];
    }
    
    self.downLoadableUIImages = [[NSMutableArray alloc] init];
    // TODO : which image to pick if there are more than one imagepair in images
    if ([cteHotel.images count] > 0) {
        for (NSString *imageURL in cteHotel.images) {
            DownloadableUIImage *downloadableUIImage = [[DownloadableUIImage alloc] init];
            downloadableUIImage.URL = [NSURL URLWithString:imageURL];
            [self.downLoadableUIImages addObject:downloadableUIImage];
        }
    }
    if ( cteHotel.lowestRate == nil && [cteHotel.availabilityErrorCode length])
        self.isAvailable = NO;
    else
        self.isAvailable = YES;
    
    // Check if there is recommendation score.
    //TODO: For first round just check score > 1 , add more logic later
    if (cteHotel.recommendationScore > 1) {
        self.cellHeight = 130;
    }
    else
    {
        self.cellHeight = 110;
    }
     return self;
}

-(instancetype)init
{
    self = [self initWithCTEHotel:nil];
    
    if (!self) {
        return nil;
    }
    return self;
}

-(CTEHotel *)getCTEHotel
{
    return self.cteHotel;
}


// download the hotel icon only
-(void)downloadHotelImageIcon:(NSOperationQueue *)operationQueue indexPath:(NSIndexPath *)indexPath delegate:(id<ImageDownloaderOperationDelegate>)delegate
{
    if (self.downloableImageIcon != nil) {
        ImageDownloaderOperation *downloadOperation = [[ImageDownloaderOperation alloc]initWithDownloadableImage:self.downloableImageIcon atIndexPath:indexPath delegate:delegate];
        
        if (operationQueue == nil) {
            // TODO : Create a seperate operationQueue here and add the operation
        }
        [operationQueue addOperation:downloadOperation];
    }
}

// for now, this method is for downloading the image pairs for a hotel
-(void)downloadHotelImages:(NSOperationQueue *)operationQueue indexPath:(NSIndexPath *)indexPath downloadableUIImage:(DownloadableUIImage*) downloadableImage delegate:(id<ImageDownloaderOperationDelegate>)delegate
{
    ImageDownloaderOperation *downloadOperation = [[ImageDownloaderOperation alloc]initWithDownloadableImage:downloadableImage atIndexPath:indexPath delegate:delegate];
    
    if (operationQueue == nil) {
        // TODO : Create a seperate operationQueue here and add the operation 
    }
    [operationQueue addOperation:downloadOperation];
}

-(HotelAnnotation *)getAnnotationWithIndex:(int)index
{
    HotelAnnotation *pointAnnotation = [[HotelAnnotation alloc] init];
    CLLocationCoordinate2D hotelCoordinate;
    hotelCoordinate.latitude = self.cteHotel.latitude;
    hotelCoordinate.longitude = self.cteHotel.longitude;
    
    pointAnnotation.coordinate = hotelCoordinate;
    pointAnnotation.hotelIndex = index;
//    pointAnnotation.title = @"Where am I?";
//    pointAnnotation.subtitle = @"I'm here!!!";
    
    return pointAnnotation;
}
@end
