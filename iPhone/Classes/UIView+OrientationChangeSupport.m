//
//  UIView+OrientationChangeSupport.m
//  ConcurAuth
//
//  Created by Wanny Morellato on 11/7/13.
//  Copyright (c) 2013 Wanny Morellato. All rights reserved.
//

NSString *const CCLayoutChangedToLandscape = @"LAYOUTCHANGEDTOLANDSCAPE";
NSString *const CCLayoutChangedToPortrait = @"LAYOUTCHANGEDTOPORTRAIT";

static char kinPortraitLayoutOnlyObjectKey;
static char kinLandscapeLayoutOnlyObjectKey;

static const NSInteger impossibleOrientation = 666;

UIInterfaceOrientation lastOrientation = impossibleOrientation;

#import <objc/runtime.h>
#import "UIView+OrientationChangeSupport.h"

@implementation UIView (OrientationChangeSupport)

-(BOOL)isHiddenRecursively{
    if (self.isHidden) {
        return self.isHidden;
    }else{
        return self.superview.isHiddenRecursively;
    }
}

-(BOOL)inPortraitLayoutOnly
{    
    return [objc_getAssociatedObject(self, &kinPortraitLayoutOnlyObjectKey) boolValue];
}
-(void)setInPortraitLayoutOnly:(BOOL)inPortraitLayoutOnly
{
    if (inPortraitLayoutOnly) {
        [[NSNotificationCenter defaultCenter] addObserverForName:CCLayoutChangedToLandscape object:nil
                                                           queue:[NSOperationQueue mainQueue]
                                                      usingBlock:^(NSNotification *notification)
         {
             [self setHidden:YES];
         }];
        [[NSNotificationCenter defaultCenter] addObserverForName:CCLayoutChangedToPortrait object:nil
                                                           queue:[NSOperationQueue mainQueue]
                                                      usingBlock:^(NSNotification *notification)
         {
             [self setHidden:NO];
             
         }];
    }
    objc_setAssociatedObject(self, &kinPortraitLayoutOnlyObjectKey, [NSNumber numberWithBool:inPortraitLayoutOnly], OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

-(BOOL)inLandscapeLayoutOnly
{
    return [objc_getAssociatedObject(self, &kinLandscapeLayoutOnlyObjectKey) boolValue];
}
-(void)setInLandscapeLayoutOnly:(BOOL)inLandscapeLayoutOnly
{
    if (inLandscapeLayoutOnly) {
        [[NSNotificationCenter defaultCenter] addObserverForName:CCLayoutChangedToLandscape object:nil
                                                           queue:[NSOperationQueue mainQueue]
                                                      usingBlock:^(NSNotification *notification)
         {
             [self setHidden:NO];
         }];
        [[NSNotificationCenter defaultCenter] addObserverForName:CCLayoutChangedToPortrait object:nil
                                                           queue:[NSOperationQueue mainQueue]
                                                      usingBlock:^(NSNotification *notification)
         {
             [self setHidden:YES];
             
         }];
    }
    objc_setAssociatedObject(self, &kinLandscapeLayoutOnlyObjectKey, [NSNumber numberWithBool:inLandscapeLayoutOnly], OBJC_ASSOCIATION_RETAIN);
}

+ (void)updateViewsToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    
    if (lastOrientation == toInterfaceOrientation) {
        // filter out multiple notificaions
        return;
    }
    
    lastOrientation = toInterfaceOrientation;
    
    if (UIInterfaceOrientationIsLandscape(toInterfaceOrientation)) {
        [[NSNotificationCenter defaultCenter] postNotificationName:CCLayoutChangedToLandscape object:self];
    }else {
        [[NSNotificationCenter defaultCenter] postNotificationName:CCLayoutChangedToPortrait object:self];
    }
}

@end

@interface UIViewOutletCollectionArray ()

@property (strong,nonatomic) NSMutableArray *backingStore;

@end

@implementation UIViewOutletCollectionArray

- (id) init
{
    self = [super init];
    if (self != nil) {
        _backingStore = [NSMutableArray new];
    }
    return self;
}

- (id) initWithCapacity:(NSUInteger)numItems
{
    [self isKindOfClass:nil];
    self = [super init];
    if (self != nil) {
        _backingStore = [[NSMutableArray alloc]initWithCapacity:numItems];
    }
    return self;
}

- (void)dealloc{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
}
#pragma mark NSObject

- (NSString*)descriptionWithLocale:(id)locale{
    return [NSString stringWithFormat:@"-- UIViewOutletCollectionArray -- %@ for %@",[self class],_backingStore];
}

#pragma mark NSArray

-(NSUInteger)count
{
    return [_backingStore count];
}

-(id)objectAtIndex:(NSUInteger)index
{
    return [_backingStore objectAtIndex:index];
}

#pragma mark NSMutableArray

-(void)insertObject:(id)anObject atIndex:(NSUInteger)index
{
    [[NSNotificationCenter defaultCenter] addObserverForName:CCLayoutChangedToLandscape object:nil
                                                       queue:[NSOperationQueue mainQueue]
                                                  usingBlock:^(NSNotification *notification)
     {
         if ([anObject isFirstResponder]) {
             if ([anObject isHiddenRecursively]) {
                 for (UIView *aSisterView in _backingStore) {
                     if (aSisterView != anObject && ![aSisterView inPortraitLayoutOnly]) {
                         [aSisterView becomeFirstResponder];
                     }
                 }
             }
         }
         
     }];
    [[NSNotificationCenter defaultCenter] addObserverForName:CCLayoutChangedToPortrait object:nil
                                                       queue:[NSOperationQueue mainQueue]
                                                  usingBlock:^(NSNotification *notification)
     {
         if ([anObject isFirstResponder]) {
             if ([anObject isHiddenRecursively]) {
                 for (UIView *aSisterView in _backingStore) {
                     if (aSisterView != anObject && ![aSisterView inLandscapeLayoutOnly]) {
                         [aSisterView becomeFirstResponder];
                     }
                 }
             }
         }
     }];
    
    [_backingStore insertObject:anObject atIndex:index];
}
-(void)removeObjectAtIndex:(NSUInteger)index
{
    [_backingStore removeObjectAtIndex:index];
}

-(void)addObject:(id)anObject
{
    [_backingStore addObject:anObject];
}

-(void)removeLastObject
{
    [_backingStore removeLastObject];
}

-(void)replaceObjectAtIndex:(NSUInteger)index withObject:(id)anObject
{
    [_backingStore replaceObjectAtIndex:index withObject:anObject];
}

#pragma behave like a the collection of class it holds

- (NSMethodSignature *)methodSignatureForSelector:(SEL)selector
{
    NSMethodSignature *signature = [_backingStore methodSignatureForSelector:selector];
    if (!signature)
    {
        if ([_backingStore count]>0) {
            signature = [_backingStore[0] methodSignatureForSelector:selector];
        } else{
            NSLog(@"-- %@ -- does not have any item inside",[self class]);
        }
    }
    return signature;
}

- (void)forwardInvocation:(NSInvocation *)invocation
{
    if ([_backingStore respondsToSelector:invocation.selector]) {
        [invocation invokeWithTarget:_backingStore];
    }else{
        for (UIView *aSisterView in _backingStore) {
            [invocation invokeWithTarget:aSisterView];
        }
    }
}


@end



// Support for UITextField

@implementation UITextFieldCouple{
}


-(void)insertObject:(id)anObject atIndex:(NSUInteger)index
{
    [super insertObject:anObject atIndex:index];
    [anObject addTarget:self action:@selector(textFieldDidChange:) forControlEvents:UIControlEventEditingChanged];
    
}


#pragma mark Synchronize textField text change

-(void)textFieldDidChange :(UITextField *)theTextField{
    for (UITextField *text in self.backingStore) {
        [text setText:theTextField.text];
    }
}




@end
