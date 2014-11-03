//
//  HotelLocationHandler.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class HotelLocationViewController;

@interface HotelLocationHandler : NSObject <UITableViewDelegate, UITableViewDataSource>
{
	HotelLocationViewController	*hotelLocationViewController;
	NSString					*mostRecentSearchText;
}

@property (nonatomic, strong) HotelLocationViewController	*hotelLocationViewController;
@property (nonatomic, strong) NSString						*mostRecentSearchText;

- (id)initWithHotelLocationViewController:(HotelLocationViewController *)vc;
- (void)becameActiveHotelLocationHandler;
- (void)doCancel;
- (void)doSearch:(NSString *)searchText;
- (void)foundResults:(NSMutableArray*)results forAddress:(NSString*)address;

@end
