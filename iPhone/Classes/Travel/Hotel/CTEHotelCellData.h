//
//  CTEHotelCellData.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

/*!
    SubClass of AbstractTableViewCellData so that the ViewController has always gets a unified CellData
 */
#import "AbstractTableViewCellData.h"
#import "CTEHotel.h"
#import "DownloadableUIImage.h"
#import "ImageDownloaderOperation.h"
#import <MapKit/MapKit.h>
@class HotelAnnotation;

@interface CTEHotelCellData : AbstractTableViewCellData
@property (nonatomic, strong) NSMutableArray *downLoadableUIImages;
@property (nonatomic, strong) DownloadableUIImage *downloableImageIcon;

@property (nonatomic, strong) CTEHotel *cteHotel;
@property BOOL isAvailable;

-(instancetype)initWithCTEHotel:(CTEHotel *)cteHotel;
-(CTEHotel *)getCTEHotel;

/*!
 Method to download the hotel image icon asynchronously in an operation queue
 @param operationQueue - operationQueue to add the downloadoperation. TODO: if nil creates a new operation
 */
-(void)downloadHotelImageIcon:(NSOperationQueue *)operationQueue
                    indexPath:(NSIndexPath *)indexPath
                     delegate:(id<ImageDownloaderOperationDelegate>)delegate;


/*!
 Method to download the hotel detail images asynchronously in an operation queue
 @param operationQueue - operationQueue to add the downloadoperation. TODO: if nil creates a new operation
 */
-(void)downloadHotelImages:(NSOperationQueue *)operationQueue
                 indexPath:(NSIndexPath *)indexPath
       downloadableUIImage:(DownloadableUIImage*) downloadableImage
                  delegate:(id<ImageDownloaderOperationDelegate>)delegate;

-(HotelAnnotation *)getAnnotationWithIndex:(int)index;


@end
