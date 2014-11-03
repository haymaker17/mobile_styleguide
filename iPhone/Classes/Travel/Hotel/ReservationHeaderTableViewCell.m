//
//  RoomReservationHeaderTableViewCell.m
//  ConcurMobile
//
//  Created by Sally Yan on 8/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ReservationHeaderTableViewCell.h"

@interface ReservationHeaderTableViewCell()
@property (nonatomic, strong) NSOperationQueue *imageDownloadQueue;
@property BOOL isHotelImageAvailable;
@end

@implementation ReservationHeaderTableViewCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (NSOperationQueue *)imageDownloadQueue {
    if (!_imageDownloadQueue) {
        _imageDownloadQueue = [[NSOperationQueue alloc] init];
        _imageDownloadQueue.name = @"Download Queue";
        // Let the OS manage
        _imageDownloadQueue.maxConcurrentOperationCount = 5;
    }
    return _imageDownloadQueue;
}

- (void)awakeFromNib
{
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void)setCellData:(ReservationHeaderCellData *)cellData
{
    RoomsListCellData *roomsListCellData = [cellData getRoomsListCellData];
    CTEHotelRate *hotelRate = [roomsListCellData getHotelRatesData];
    self.roomDescription.text = hotelRate.roomDescription;
    
    CTEHotelCellData *hotelCellData = [cellData getCTEHotelCellData];
    [self setRoomImage:hotelCellData];
    
    // if the big hotel image is not available, hide the image view
    if (self.imageViewRoomImage.image == nil) {
        [self.imageViewRoomImage setHidden:YES];
        cellData.cellHeight = 93.0;
        self.coTopSpaceToRoomDescription.constant = 5;
    }
    
}

-(void)setRoomImage:(CTEHotelCellData *)hotelCellData
{
    if ([hotelCellData.downLoadableUIImages count] > 0) {
        for (int i = 0; i < [hotelCellData.downLoadableUIImages count]; i++) {
            DownloadableUIImage *downloadableUIImage = hotelCellData.downLoadableUIImages[i];
            if ([downloadableUIImage hasImage]) {
                // get hotel image icon
                if (downloadableUIImage.image != nil && !self.isHotelImageAvailable) {
                    // check if it is the big image
                    if ([self isImageWidthGreaterThanHeight:downloadableUIImage.image]) {
                        self.imageViewRoomImage.image = downloadableUIImage.image;
                        self.isHotelImageAvailable = YES;
                        break;
                    }
                }
            }
            else if ([downloadableUIImage isFailed]){
                DLog(@"Hotel Image url download Failed ");
            }
            else{
                if (downloadableUIImage.URL != nil) {
                    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:i inSection:0];
                    [hotelCellData downloadHotelImages:self.imageDownloadQueue indexPath:indexPath downloadableUIImage:downloadableUIImage delegate:self];
                }
            }
        }
    }
}

-(BOOL)isImageWidthGreaterThanHeight:(UIImage *)image
{
    float imageWidth = image.size.width;
    float imageHeight = image.size.height;
    if (imageWidth > imageHeight) {
        return YES;
    }
    return NO;
}

#pragma mark - ImageDownloaderOperationDelegate
-(void)ImageDownloaderOperationDidFinish:(ImageDownloaderOperation *)downloader
{
    // get the big image to display
    if (!self.isHotelImageAvailable) {
        if (!self.isHotelImageAvailable && downloader.downloadableImage.image != nil) {
            if ([self isImageWidthGreaterThanHeight:downloader.downloadableImage.image]) {
                self.imageViewRoomImage.image = downloader.downloadableImage.image;
                self.isHotelImageAvailable = YES;
            }
        }
    }
}

-(void)dealloc
{
    [self.imageDownloadQueue setSuspended:YES];
}


@end
