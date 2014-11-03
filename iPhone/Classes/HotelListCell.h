//
//  HotelListCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/23/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@class HotelCollectionViewController;
@class AsyncImageView;


@interface HotelListCell : UITableViewCell
{
	HotelCollectionViewController	*__weak parentMVC;
	NSUInteger						hotelIndex;
	UIImageView						*logoView;
	UILabel							*name;
	UILabel							*address1;
	UILabel							*address2;
	UILabel							*address3;
	UILabel							*phone;
	UILabel							*distance;
	UILabel							*amount;
	UILabel							*starRating;
	UILabel							*shadowStarRating;
	UILabel							*notRated;
    
    UILabel                         *lblStarting;
    UIImageView                     *ivStars, *ivDiamonds;
}

@property (nonatomic, weak) HotelCollectionViewController	*parentMVC;
@property (nonatomic) NSUInteger							hotelIndex;
@property (nonatomic, strong) IBOutlet UIImageView			*logoView;
@property (nonatomic, strong) IBOutlet UILabel				*name;
@property (nonatomic, strong) IBOutlet UILabel				*address1;
@property (nonatomic, strong) IBOutlet UILabel				*address2;
@property (nonatomic, strong) IBOutlet UILabel				*address3;
@property (nonatomic, strong) IBOutlet UILabel				*phone;
@property (nonatomic, strong) IBOutlet UILabel				*distance;
@property (nonatomic, strong) IBOutlet UILabel				*amount;
@property (nonatomic, strong) IBOutlet UILabel				*starRating;
@property (nonatomic, strong) IBOutlet UILabel				*shadowStarRating;
@property (nonatomic, strong) IBOutlet UILabel				*notRated;
@property (strong, nonatomic) IBOutlet UILabel              *recommendationText;
@property (strong, nonatomic) IBOutlet UILabel              *lblTravelPoints;

@property (nonatomic, strong) IBOutlet UILabel              *lblStarting;
@property (nonatomic, strong) IBOutlet UIImageView          *ivStars;
@property (nonatomic, strong) IBOutlet UIImageView          *ivDiamonds;
@property (strong, nonatomic) IBOutlet UIImageView          *ivRecommendation;
@property (strong, nonatomic) IBOutlet UIView               *isFedRoomView;

-(IBAction)btnAddress:(id)sender;
-(IBAction)btnPhone:(id)sender;



@end
