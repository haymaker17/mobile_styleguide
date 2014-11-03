//
//  UIView+OrientationChangeSupport.h
//  ConcurAuth
//
//  Created by Wanny Morellato on 11/7/13.
//  Copyright (c) 2013 Wanny Morellato. All rights reserved.
//

#import <UIKit/UIKit.h>

extern NSString *const CCLayoutChangedToLandscape;
extern NSString *const CCLayoutChangedToPortrait;

/*
 * This classes provide support for swapping between views on orientation changes
 * also call + (void)updateViewsToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration;
 * in your view controller
 *     - (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration{
 *
 *            [UIView updateViewsToInterfaceOrientation:toInterfaceOrientation duration:duration];
 *     }
 */

@interface UIView (OrientationChangeSupport)

// please set inPortraitLayoutOnly = YES for the view that only appears in Portraits
@property BOOL inPortraitLayoutOnly;
// please set inLandscapeLayoutOnly = YES for the view that only appears in Landscape
@property BOOL inLandscapeLayoutOnly;

// return if the view is hidden either because of itself or because of one of its superview
@property(readonly) BOOL isHiddenRecursively;

// please inform UIView (OrientationChangeSupport) by calling the following method
+ (void)updateViewsToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration;

@end

//IBOutletCollection(<#ClassName#>) for generic UIView deal with firstResponder
@interface UIViewOutletCollectionArray : NSMutableArray

@end

//IBOutletCollection(<#ClassName#>) for UITextField deal with text changes
@interface UITextFieldCouple : UIViewOutletCollectionArray

@end