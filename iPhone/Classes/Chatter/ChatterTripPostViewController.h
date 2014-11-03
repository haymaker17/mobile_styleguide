//
//  ChatterTripPostViewController.h
//  ConcurMobile
//
//  Created by ernest cho on 6/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChatterTripPostViewController : MobileViewController <ExMsgRespondDelegate>

- (id)initWithTripDescription:(NSString *)tripDescription recordLocator:(NSString *)recordLocator;
@end
