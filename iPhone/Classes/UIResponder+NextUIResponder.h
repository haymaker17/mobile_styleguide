//
//  UIResponder+NextResponder.h
//  ConcurAuth
//
//  Created by Wanny Morellato on 11/11/13.
//  Copyright (c) 2013 Wanny Morellato. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIResponder (NextUIResponder)

@property(retain, nonatomic) IBOutlet UIResponder* nextUIResponder;

- (BOOL)becomeFirstUIResponder;

@end
