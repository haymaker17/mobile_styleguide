//
//  HotelSearchTableViewCell.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelSearchTableViewCell.h"

@interface HotelSearchTableViewCell()
@property (nonatomic, strong) NSOperationQueue *imageDownloadQueue;
@end

@implementation HotelSearchTableViewCell

- (void)awakeFromNib
{
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
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


-(void)setCellData:(CTEHotelCellData *)cteHotelData indexPath:(NSIndexPath *)indexPath
{

    self.isCellEnabled = YES;
    CTEHotel *cteHotel = [cteHotelData getCTEHotel];
    self.hotelName.text = cteHotel.propertyName;        // Default: FadeTruncatingTail
    self.hotelAvailability.hidden = YES;
    // there's no addressLine2 anymore
    if ([cteHotel.addressLine1 lengthIgnoreWhitespace] > 0)
    {
        self.hotelCityAndState.text = [NSString stringWithFormat:@"%@ %@", cteHotel.addressLine1, cteHotel.city];
    }
    else{
        self.hotelCityAndState.text = [NSString stringWithFormat:@"%@", cteHotel.city];
    }
//    self.hotelCityAndState.text = cteHotel.addressLine1 == nil ? cteHotel.addressLine2 : cteHotel.addressLine1;
    self.hotelDistance.text = [NSString stringWithFormat:@"%.2lf %@",cteHotel.distance, [Localizer getLocalizedText:@"miles"]];
    
    // the if statement is just for now, temporary, will need to remove after the polling stuffs get completed!
    if (cteHotel.lowestRate) {
        self.hotelPrice.text = [FormatUtils formatMoneyString:cteHotel.lowestRate crnCode:cteHotel.currency decimalPlaces:0];
        self.hotelPrice.textColor = [UIColor concurBlueColor];
    }
    else if ([cteHotel.availabilityErrorCode length]) {
        self.hotelAvailability.text = [cteHotel.availabilityErrorCode isEqualToString:@"PropertyNotAvailable"] ? [@"Sold Out" localize] : [@"Not Available" localize];
        self.isCellEnabled = cteHotelData.isAvailable;
    }
    else {
        self.hotelPrice.text = @"";//[@"View Rates" localize];
//        self.hotelPrice.textColor = [UIColor darkGrayConcur];
    }
    
     // get the rating stars image
    if (cteHotel.starRating == 0){
        self.ivHotelStarRating.hidden = YES;
    }
    else
    {
        self.ivHotelStarRating.hidden = NO;
        if(cteHotel.starRating == 1)
            self.ivHotelStarRating.image = [UIImage imageNamed:@"hotel_one_star"];
        else if(cteHotel.starRating == 2)
            self.ivHotelStarRating.image = [UIImage imageNamed:@"hotel_two_star"];
        else if(cteHotel.starRating == 3)
            self.ivHotelStarRating.image = [UIImage imageNamed:@"hotel_three_star"];
        else if(cteHotel.starRating == 4)
            self.ivHotelStarRating.image = [UIImage imageNamed:@"hotel_four_star"];
        else if(cteHotel.starRating == 5)
            self.ivHotelStarRating.image = [UIImage imageNamed:@"hotel_five_star"];
    }
    
    /*
     The labeling User story is here MOB-20319
     Suggested based on a past stay
     Suggested based on co-workers
     Suggested based on popularity
     
     TODO : We will show 5 results for recommendations for every sort. Properties are ordered based on the sort. The recommendations are displayed for the first 5 properties that are tied to a recommendation and are labeled appropriately.
     We will now have a sort by “suggested” . We will default to that sort.

     */
    if (cteHotel.recommendationScore > 1 ) {
        self.hotelSuggestedText.hidden = NO;

        if ([cteHotel.recommendationDescription isEqualToString:@"PersonalHistory"]) {
            self.hotelSuggestedText.text = [@"Suggested based on past stay" localize];
        }
        else if ([cteHotel.recommendationDescription isEqualToString:@"CompanyFavorite"]) {
            self.hotelSuggestedText.text = [@"Suggested based on co-worker" localize];
        }
        else if ([cteHotel.recommendationDescription isEqualToString:@"Algorithm"]) {
            self.hotelSuggestedText.text = [@"Suggested based on popularity" localize];
        }
        else {
            self.hotelSuggestedText.hidden = YES;
        }
    }
    else
    {
        self.hotelSuggestedText.hidden = YES;
    }
    
    self.hotelAvailability.hidden = cteHotelData.isAvailable;   // hide if
    self.hotelPrice.hidden = !cteHotelData.isAvailable;
    
    [self setHotelImageIcon:cteHotelData indexPath:indexPath];
    // Show preferred only if the
    self.hotelPreferred.hidden = (cteHotel.companyPreference < CTEHotelPreferenceRankLessPreferred) || ! cteHotelData.isAvailable;
    // MOB-21732 Set background color on Preferred label
    [self.hotelPreferred setPersistentBackgroundColor:[UIColor backgroundForMainButtonWorkflow]];
    [self displayCellAsEnabled:self.isCellEnabled];
}

-(void)setHotelImageIcon:(CTEHotelCellData *)cteHotelData indexPath:(NSIndexPath *)indexPath
{
    if (cteHotelData.downloableImageIcon != nil) {
        DownloadableUIImage* downloadableImage = cteHotelData.downloableImageIcon;
        if ([downloadableImage hasImage]) {
            self.hotelImage.image = downloadableImage.image;
        }
        else if ([downloadableImage isFailed])
        {
            // Show a static image if image download failed
            DLog(@"Hotel Image url download Failed ");
            self.hotelImage.image =  [UIImage imageNamed:@"home_icon_hotel"];
        }
        else
        {
            // if its not there yet then start the download operation.
            if (downloadableImage.URL != nil) {
                [cteHotelData downloadHotelImageIcon:self.imageDownloadQueue indexPath:indexPath delegate:self];
            }
            else
            {
                // hotel image url is not there so show some static image
                DLog(@"Hotel Image url is nil ");
                self.hotelImage.image =  [UIImage imageNamed:@"home_icon_hotel"];
            }
        }
    }
}

// format the amount with currency code
-(NSString *)getHotelRateWithCurrencyCode:(NSString *)currencyCode amount:(double)amount
{
    NSString *hotelRate = nil;
    NSString *amountStr = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", amount] crnCode:currencyCode];
    NSRange range = [amountStr rangeOfString:@"."];
    if (range.location != NSNotFound) {
        hotelRate = [amountStr substringToIndex:range.location];
    }
    return hotelRate;
}

-(void)displayCellAsEnabled:(BOOL)enabled
{
    
    if (enabled && self.isCellEnabled) {
        self.userInteractionEnabled = YES;
        self.hotelPreferred.alpha = self.ivHotelStarRating.alpha = self.hotelName.alpha = self.hotelPrice.alpha = self.hotelDistance.alpha = self.hotelCityAndState.alpha = self.hotelImage.alpha = 1.0;
        self.hotelName.enabled = NO;
        self.hotelImage.alpha = 1.0;
    }
    else
    {
        self.userInteractionEnabled = NO;
        self.hotelPreferred.alpha = self.ivHotelStarRating.alpha = self.hotelName.alpha = self.hotelPrice.alpha = self.hotelDistance.alpha = self.hotelCityAndState.alpha = self.hotelImage.alpha = 0.439216f;
        self.hotelName.enabled = YES;
    }

}

#pragma mark - ImageDownloaderOperationDelegate
-(void)ImageDownloaderOperationDidFinish:(ImageDownloaderOperation *)downloader
{
    self.hotelImage.image = downloader.downloadableImage.image;
}

-(void)dealloc
{
    [self.imageDownloadQueue cancelAllOperations];
}

@end
