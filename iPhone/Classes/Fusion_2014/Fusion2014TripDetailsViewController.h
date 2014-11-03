//
//  Fusion2014TripDetailsViewController.h
//  ConcurMobile
//
//  Created by Shifan Wu on 4/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import <MapKit/MapKit.h>

@interface Fusion2014TripDetailsViewController : MobileViewController<UITableViewDataSource, UITableViewDelegate, MKMapViewDelegate, UIScrollViewDelegate>

@property (strong, nonatomic) IBOutlet UITableView *tableView;
@property (strong, nonatomic) IBOutlet UILabel *lblTripName;
@property (strong, nonatomic) IBOutlet UILabel *lblTripDateRange;
@property (strong, nonatomic) IBOutlet UIImageView *ivTripBackground;

@property (nonatomic, strong) NSString *tripDetailsRequestId;

- (void)displayTrip:(EntityTrip *)newTrip TripKey:(NSString *)newTripKey;
- (void) loadTrip:(NSMutableDictionary *)pBag;

@end
