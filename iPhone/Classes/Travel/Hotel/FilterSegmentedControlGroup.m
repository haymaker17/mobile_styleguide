//
//  FilterSegmentedControlGroup.m
//  ConcurMobile
//
//  Created by Ray Chi on 9/17/14.
//  Copyright (c) 2014 ConcurTech. All rights reserved.
//

#import "FilterSegmentedControlGroup.h"
#import "FilterCheckBox.h"


#define Width 320.0f;
#define Height 73.0f;

@interface FilterSegmentedControlGroup ()

- (void)handleSwitchEvent:(id)sender;
@property (nonatomic) NSInteger *preSelectedNumber;

-(CGRect)roundRect:(CGRect)frameOrBound;

@end

@implementation FilterSegmentedControlGroup

#pragma mark ------ Rewrite Init build in funtion
- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:[self roundRect:frame]];
    if (self) {
        self.preSelectedNumber = (NSInteger *)-1;
        UILabel *upperLine = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 320, 1)];
        upperLine.backgroundColor = [UIColor colorWithRed:167/255.0 green:182/255.0 blue:191/255.0 alpha:0.3];
        UILabel *bottomLine =[[UILabel alloc] initWithFrame:CGRectMake(0, 72.0, 320, 1)];
        bottomLine.backgroundColor = [UIColor colorWithRed:167/255.0 green:182/255.0 blue:191/255.0 alpha:0.3];
        
        [self addSubview:upperLine];
        [self addSubview:bottomLine];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if(self){
        self.preSelectedNumber = (NSInteger *)-1;
    }
    return self;
}

-(CGRect)roundRect:(CGRect)frameOrBound
{
    CGRect newRect = frameOrBound;
    newRect.size.width = Width;
    newRect.size.height = Height;
    
    return newRect;
    
}

- (void)commonInit
{
    for (UIView *control in self.subviews) {
        if ([control isKindOfClass:[FilterCheckBox class]]) {
            [(FilterCheckBox*)control addTarget:self action:@selector(handleSwitchEvent:) forControlEvents:UIControlEventValueChanged];
        }
    }
}


#pragma mark ----- Initiate function
/**
 *  Change type of the control group
 *
    type 1----- Rating
         2----- Miles
 */
- (void)changeType:(NSInteger)type
{
    switch (type) {
        case 1:{                // Change to Rating Group
            [self initStarControl];
            break;
        }
        case 2:{                // Change to Miles Group
            [self initMileControl];
            break;
        }
        default:
            break;
    }
    
    [self commonInit];
    
}

- (void) initStarControl
{
    // All
    FilterCheckBox *checkboxAll = [[FilterCheckBox alloc] initWithFrame:CGRectMake(14.5, 12, 0, 0)];
    checkboxAll.value = 0;
    
    // 3 star
    FilterCheckBox *checkbox_3star = [[FilterCheckBox alloc] initWithFrame:CGRectMake(14.5+69+5, 12, 0, 0)];
    [checkbox_3star setType:2];
    checkbox_3star.value = 3;
    
    //4 star
    FilterCheckBox *checkbox_4star = [[FilterCheckBox alloc] initWithFrame:CGRectMake(14.5+69*2+5*2, 12, 0, 0)];
    [checkbox_4star setType:2];
    [checkbox_4star changeImageStars:4];
    checkbox_4star.text = @"4+";
    checkbox_4star.value = 4;
    
    // 5 star
    FilterCheckBox *checkbox_5star = [[FilterCheckBox alloc] initWithFrame:CGRectMake(14.5+69*3+5*3, 12, 0, 0)];
    [checkbox_5star setType:2];
    [checkbox_5star changeImageStars:5];
    checkbox_5star.text = @"5";
    checkbox_5star.value = 5;
    
    [self addSubview:checkboxAll];
    [self addSubview:checkbox_3star];
    [self addSubview:checkbox_4star];
    [self addSubview:checkbox_5star];
    [self commonInit];
//    self.selectIndex = 0;
}

- (void) initMileControl
{
    // All
    FilterCheckBox *checkboxAll = [[FilterCheckBox alloc] initWithFrame:CGRectMake(14.5, 12, 0, 0)];
    checkboxAll.value = 0;
    
    // 5 miles
    FilterCheckBox *checkbox_5miles = [[FilterCheckBox alloc] initWithFrame:CGRectMake(14.5+69+5, 12, 0, 0)];
    [checkbox_5miles setType:3];
    checkbox_5miles.text = @"5 miles";
    checkbox_5miles.value = 5;
    
    // 15 miles
    FilterCheckBox *checkbox_15miles = [[FilterCheckBox alloc] initWithFrame:CGRectMake(14.5+69*2+5*2, 12, 0, 0)];
    [checkbox_15miles setType:3];
    checkbox_15miles.text = @"15 miles";
    checkbox_15miles.value = 15;
    
    // 25 miles
    FilterCheckBox *checkbox_25miles = [[FilterCheckBox alloc] initWithFrame:CGRectMake(14.5+69*3+5*3, 12, 0, 0)];
    [checkbox_25miles setType:3];
    checkbox_25miles.text = @"25 miles";
    checkbox_25miles.value = 25;
    
    [self addSubview:checkboxAll];
    [self addSubview:checkbox_5miles];
    [self addSubview:checkbox_15miles];
    [self addSubview:checkbox_25miles];
    [self commonInit];
//    self.selectIndex = 0;
    
}




#pragma mark ----- Setter of Index
- (void)setSelectIndex:(NSInteger)selectIndex
{
    for (UIView *control in self.subviews) {
        if ([control isKindOfClass:[FilterCheckBox class]]) {
            if (((FilterCheckBox*)control).value == selectIndex) {
                [(FilterCheckBox*)control setOn:YES];
                [(FilterCheckBox*)control setIsClick:YES];
            }
        }
    }
    
}

#pragma mark ----- Segment control handler

- (void)handleSwitchEvent:(id)sender
{
    FilterCheckBox *input = (FilterCheckBox*)sender;
    
    for(UIView *control in self.subviews){
        if([control isKindOfClass:[FilterCheckBox class]]){
            FilterCheckBox *tmp = (FilterCheckBox*)control;
            
            if(tmp.value==input.value){         //Currently Selected
                if((NSInteger*)tmp.value!=self.preSelectedNumber){
                    
                    if(self.onSelected){
                        self.onSelected(tmp.value);
                    }
                }
                if(!tmp.isClick){
                    continue;
                }
            }
            else if(tmp.isClick){               //previous selected
                
                self.preSelectedNumber = (NSInteger*)tmp.value;
                [tmp setIsClick:NO];
                if(tmp.isOn){
                    [tmp setOn:NO];
                }
            }
        }
    }
    
}

@end
