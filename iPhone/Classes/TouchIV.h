//
//  TouchIV.h
//  ConcurMobile
//
//  Created by Paul Kramer on 9/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
@class iPadImageViewerVC;

@interface TouchIV : UIImageView {
	CGFloat					initialDistance;
	iPadImageViewerVC		*parentVC;
}

@property (strong, nonatomic) iPadImageViewerVC		*parentVC;

-(void)callParentExpandBack:(id)sender;
@end
