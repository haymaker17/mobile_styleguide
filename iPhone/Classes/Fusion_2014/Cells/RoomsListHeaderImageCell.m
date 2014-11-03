//
//  Fusion14HotelRoomDetailsTableViewHeaderCell.m
//  ConcurMobile
//
//  Created by Sally Yan on 4/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RoomsListHeaderImageCell.h"

@interface RoomsListHeaderImageCell()
@property (nonatomic, strong) NSOperationQueue *imageDownloadQueue;
@property BOOL isHotelImageAvailable;
@end

@implementation RoomsListHeaderImageCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)awakeFromNib
{
    // set the Rooms is selected as default
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

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void)setCellData:(RoomsListHeaderImageCellData *)cellData
{
    CTEHotelCellData *hotelData = [cellData getHotelData];
    [self setHotelImage:hotelData];
    
    // if the big hotel image is not available, hide the image view
//    if (self.largeHotelImage.image == nil) {
//        [self.largeHotelImage setHidden:YES];
//        self.coTopSpaceToHotelContent.constant = 5;
//    }
}

-(void)setHotelImage:(CTEHotelCellData *)hotelCellData
{
    if ([hotelCellData.downLoadableUIImages count] > 0) {
        for (int i = 0; i < [hotelCellData.downLoadableUIImages count]; i++) {
            if (!self.isHotelImageAvailable) {
                DownloadableUIImage *downloadableUIImage = hotelCellData.downLoadableUIImages[i];
                if ([downloadableUIImage hasImage]) {
                    [self cropImage:downloadableUIImage.image];
                    self.isHotelImageAvailable = YES;
                    break;
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
            } // end of if (!self.isHotelImageAvailable)
        } // end of for loop
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


-(void)cropImage:(UIImage *)image
{
    UIImageView *imageView = [[UIImageView alloc] initWithImage:image];
    float originX = imageView.center.x - self.largeHotelImage.frame.size.width / 2;
    float originY = imageView.center.y - self.largeHotelImage.frame.size.height / 2 - 60;
    
    CGRect clippedRect  = CGRectMake(originX, originY, self.largeHotelImage.frame.size.width, self.largeHotelImage.frame.size.height + 60);
    CGImageRef imageRef = CGImageCreateWithImageInRect([image CGImage], clippedRect);
    UIImage *newImage   = [UIImage imageWithCGImage:imageRef];
    self.largeHotelImage.image = newImage;
    CGImageRelease(imageRef);
}

#pragma mark - ImageDownloaderOperationDelegate
-(void)ImageDownloaderOperationDidFinish:(ImageDownloaderOperation *)downloader
{
    // get the big image to display
    if (!self.isHotelImageAvailable && downloader.downloadableImage.image != nil)
    {
        [self cropImage:downloader.downloadableImage.image];
        self.isHotelImageAvailable = YES;
    }
}

-(void)dealloc
{
    [self.imageDownloadQueue cancelAllOperations];
}

@end
