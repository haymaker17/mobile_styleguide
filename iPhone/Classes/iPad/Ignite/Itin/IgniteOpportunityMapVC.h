//
//  IgniteOpportunityMapVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 9/5/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IgniteVendorAnnotation.h"
#import "EntityTrip.h"
#import "IgniteSegmentEditDelegate.h"
#import "IgnitePopoverModalDelegate.h"
#import "EntitySegment.h"

@interface IgniteOpportunityMapVC : MobileViewController<MKMapViewDelegate,
    IgniteSegmentEditDelegate, IgnitePopoverModalDelegate>

{
    MKMapView                       *mapView;
    IgniteVendorAnnotation          *currentLoc;
    
    EntityTrip                      *trip;
    EntitySegment                   *segment;
    id<IgniteSegmentEditDelegate>   __weak _delegate;
    NSMutableArray                  *lstOpportunityId; // list of opportunity ids
}

@property (nonatomic, strong) IBOutlet MKMapView            *mapView;
@property (nonatomic, strong) EntityTrip                    *trip;
@property (nonatomic, strong) EntitySegment                 *segment;
@property (nonatomic, strong) IgniteVendorAnnotation        *currentLoc;
@property (nonatomic, strong) NSMutableArray                *lstOpportunityId;
@property (nonatomic, weak) id<IgniteSegmentEditDelegate> delegate;

@end
