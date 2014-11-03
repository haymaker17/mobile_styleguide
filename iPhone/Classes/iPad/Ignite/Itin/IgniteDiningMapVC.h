//
//  IgniteDiningMapVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/23/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "IgniteVendorAnnotation.h"
#import "EntitySegment.h"
#import "IgniteSegmentEditDelegate.h"

@interface IgniteDiningMapVC : MobileViewController<MKMapViewDelegate,
    UITableViewDataSource, UITableViewDelegate,
    IgniteSegmentEditDelegate>
{
    MKMapView                       *mapView;
    UIView                          *vwCuisine;
    UITableView                     *tblCuisinePicker;
    IgniteVendorAnnotation          *currentLoc;
    
    NSArray                         *lstCuisine;
    NSString                        *selectedCuisine;
    
    EntitySegment                   *segment;
    id<IgniteSegmentEditDelegate>   __weak _delegate;
    
    UIPopoverController             *vcPopover;
}

@property (nonatomic, strong) IBOutlet MKMapView            *mapView;
@property (nonatomic, strong) IBOutlet UIView               *vwCuisine;
@property (nonatomic, strong) IBOutlet UITableView          *tblCuisinePicker;
@property (nonatomic, strong) IgniteVendorAnnotation        *currentLoc;
@property (nonatomic, strong) NSArray                       *lstCuisine;
@property (nonatomic, strong) NSString                      *selectedCuisine;
@property (nonatomic, strong) EntitySegment                 *segment;
@property (nonatomic, weak) id<IgniteSegmentEditDelegate> delegate;
@property (nonatomic, strong) UIPopoverController           *vcPopover;

- (void)setSeedData:(id<IgniteSegmentEditDelegate>) del loc:(IgniteVendorAnnotation*)annotation seg:(EntitySegment*) seg;

@end
