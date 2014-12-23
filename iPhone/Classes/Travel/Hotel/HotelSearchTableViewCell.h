//
//  HotelSearchTableViewCell.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTEHotelCellData.h"
#import "FadeTruncatingLabel.h"
#import "PersistentBackgroundLabel.h"

@interface HotelSearchTableViewCell : UITableViewCell <ImageDownloaderOperationDelegate>

@property (weak, nonatomic) IBOutlet UIImageView *hotelImage;
@property (weak, nonatomic) IBOutlet UIImageView *ivHotelStarRating;
@property (weak, nonatomic) IBOutlet UILabel *hotelName;
@property (weak, nonatomic) IBOutlet UILabel *hotelCityAndState;
@property (weak, nonatomic) IBOutlet UILabel *hotelDistance;
@property (weak, nonatomic) IBOutlet UILabel *hotelPrice;
@property (weak, nonatomic) IBOutlet PersistentBackgroundLabel *hotelPreferred;
@property (weak, nonatomic) IBOutlet UILabel *travelPoints;
@property (weak, nonatomic) IBOutlet UILabel *hotelAvailability;
@property (weak, nonatomic) IBOutlet UILabel *hotelSuggestedText;
@property BOOL isCellEnabled;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coHotelPreferredWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coHotelPreferredHeight;

-(void)setCellData:(CTEHotelCellData *)cteHotelData indexPath:(NSIndexPath *)indexPath;

-(void)displayCellAsEnabled:(BOOL)enabled;

@end
