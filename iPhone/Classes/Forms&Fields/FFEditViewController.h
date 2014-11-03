//
//  FFEditViewController.h
//  ConcurMobile
//
//  Created by laurent mery on 31/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTEField.h"
#import "FFEditViewProtocol.h"

@interface FFEditViewController : UIViewController

//bridge datas
@property (nonatomic, copy) NSString *label;
@property (nonatomic, retain) CTEField *field;
@property (nonatomic, copy) NSString *value;

//to save value
@property (nonatomic, weak) id<FFEditViewProtocol> delegate;

@end
