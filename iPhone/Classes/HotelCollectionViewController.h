//
//  HotelCollectionViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"

@class HotelSearchResultsViewController;
@class HotelListCell;
@class HotelResult;
#import "HotelSearch.h"

@interface HotelCollectionViewController : MobileViewController
{
	HotelSearchResultsViewController*	__weak parentMVC;
    NSString *hotelSearchCriteria;
    HotelSearch				*hotelSearch;
}
@property (nonatomic, strong) HotelSearch				*hotelSearch;
@property (nonatomic, strong) NSString *hotelSearchCriteria;
@property (nonatomic, weak) HotelSearchResultsViewController	*parentMVC;

-(void)notifyChange;

-(void)didSwitchViews;

-(void)addressPressed:(id)sender;
-(void)phonePressed:(id)sender;

- (IBAction)buttonReorderPressed:(id)sender;
- (IBAction)buttonActionPressed:(id)sender;

- (HotelListCell *)makeAndConfigureHotelListCellForTable:(UITableView *)tableView hotel:(HotelResult*)hotelResult;

- (void)updateToolbar;

@end
